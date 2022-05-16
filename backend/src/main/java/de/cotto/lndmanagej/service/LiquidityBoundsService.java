package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.LiquidityBounds;
import de.cotto.lndmanagej.model.LiquidityBoundsWithTimestamp;
import de.cotto.lndmanagej.model.Pubkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.LIQUIDITY_INFORMATION_MAX_AGE;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.USE_MISSION_CONTROL;
import static de.cotto.lndmanagej.model.LiquidityBounds.NO_INFORMATION;

@Component
public class LiquidityBoundsService {
    private static final int ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1_000;
    private static final Duration DEFAULT_MAX_AGE = Duration.of(10, ChronoUnit.MINUTES);
    private final MissionControlService missionControlService;
    private final Map<TwoPubkeys, LiquidityBoundsWithTimestamp> entries;
    private final ConfigurationService configurationService;
    private final LoadingCache<Object, Duration> maxAgeCache = new CacheBuilder()
            .withRefresh(Duration.ofSeconds(5))
            .withExpiry(Duration.ofSeconds(10))
            .build(this::getMaxAgeWithoutCache);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public LiquidityBoundsService(
            MissionControlService missionControlService,
            ConfigurationService configurationService
    ) {
        this.missionControlService = missionControlService;
        this.configurationService = configurationService;
        entries = new LinkedHashMap<>();
    }

    @Timed
    public Optional<Coins> getAssumedLiquidityUpperBound(Edge edge) {
        Pubkey source = edge.startNode();
        Pubkey target = edge.endNode();
        Coins fromMissionControl = getFromMissionControl(source, target).orElse(null);
        Coins fromPayments = getInfo(new TwoPubkeys(source, target)).getUpperBound().orElse(null);
        if (fromMissionControl == null && fromPayments == null) {
            return Optional.empty();
        }
        Coins maxHtlc = edge.policy().maxHtlc();
        if (fromMissionControl == null) {
            return Optional.of(Objects.requireNonNull(fromPayments).minimum(maxHtlc));
        }
        Coins oneSatBelowFailure = fromMissionControl.subtract(Coins.ofSatoshis(1)).minimum(maxHtlc);
        if (fromPayments == null) {
            return Optional.of(oneSatBelowFailure);
        }
        return Optional.of(oneSatBelowFailure.minimum(fromPayments));
    }

    @Timed
    public Coins getAssumedLiquidityLowerBound(Edge edge) {
        LiquidityBounds info = getInfo(new TwoPubkeys(edge.startNode(), edge.endNode()));
        return info.getLowerBound().minimum(edge.policy().maxHtlc());
    }

    public void markAsMoved(Pubkey source, Pubkey target, Coins amount) {
        update(source, target, amount, LiquidityBounds::withMovedCoins);
    }

    public void markAsAvailable(Pubkey source, Pubkey target, Coins amount) {
        update(source, target, amount, LiquidityBounds::withAvailableCoins);
    }

    public void markAsUnavailable(Pubkey source, Pubkey target, Coins amount) {
        update(source, target, amount, LiquidityBounds::withUnavailableCoins);
    }

    public void markAsInFlight(Pubkey source, Pubkey target, Coins amount) {
        update(source, target, amount, LiquidityBounds::withAdditionalInFlight);
    }

    @Scheduled(fixedDelay = ONE_HOUR_IN_MILLISECONDS)
    public void cleanup() {
        logger.debug("Number of entries before cleanup: {}", entries.size());
        TemporalAmount maxAge = getMaxAge();
        entries.values().removeIf(liquidityBoundsWithTimestamp -> liquidityBoundsWithTimestamp.isTooOld(maxAge));
        logger.debug("Number of entries after cleanup: {}", entries.size());
    }

    private void update(
            Pubkey source,
            Pubkey target,
            Coins amount,
            BiFunction<LiquidityBounds, Coins, Optional<LiquidityBounds>> function
    ) {
        TwoPubkeys twoPubkeys = new TwoPubkeys(source, target);
        synchronized (this.entries) {
            Optional<LiquidityBounds> updated = function.apply(getInfo(twoPubkeys), amount);
            updated.ifPresent(liquidityBounds -> setInfo(twoPubkeys, liquidityBounds));
        }
    }

    private LiquidityBounds getInfo(TwoPubkeys twoPubkeys) {
        LiquidityBoundsWithTimestamp liquidityBoundsWithTimestamp = entries.get(twoPubkeys);
        if (liquidityBoundsWithTimestamp == null) {
            return NO_INFORMATION;
        }
        if (liquidityBoundsWithTimestamp.isTooOld(getMaxAge())) {
            entries.remove(twoPubkeys);
            return NO_INFORMATION;
        }
        return liquidityBoundsWithTimestamp.liquidityBounds();
    }

    private void setInfo(TwoPubkeys twoPubkeys, LiquidityBounds liquidityBounds) {
        if (NO_INFORMATION.equals(liquidityBounds)) {
            entries.remove(twoPubkeys);
        } else {
            entries.put(twoPubkeys, new LiquidityBoundsWithTimestamp(liquidityBounds));
        }
    }

    private Optional<Coins> getFromMissionControl(Pubkey source, Pubkey target) {
        boolean useMissionControl = configurationService.getBooleanValue(USE_MISSION_CONTROL).orElse(false);
        if (useMissionControl) {
            return missionControlService.getMinimumOfRecentFailures(source, target);
        }
        return Optional.empty();
    }

    @VisibleForTesting
    record TwoPubkeys(Pubkey source, Pubkey target) {
    }

    private Duration getMaxAge() {
        return maxAgeCache.get("");
    }

    private Duration getMaxAgeWithoutCache() {
        return configurationService.getIntegerValue(LIQUIDITY_INFORMATION_MAX_AGE)
                .map(Duration::ofSeconds)
                .orElse(DEFAULT_MAX_AGE);
    }
}
