package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LiquidityBounds;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.LIQUIDITY_INFORMATION_MAX_AGE;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.USE_MISSION_CONTROL;

@Component
public class LiquidityBoundsService {
    private final MissionControlService missionControlService;
    private final LoadingCache<TwoPubkeys, LiquidityBounds> entries;
    private final ConfigurationService configurationService;

    public LiquidityBoundsService(
            MissionControlService missionControlService,
            ConfigurationService configurationService
    ) {
        this.missionControlService = missionControlService;
        this.configurationService = configurationService;
        entries = new CacheBuilder()
                .withSoftValues(true)
                .build(ignored -> getLiquidityBounds());
    }

    @Timed
    public Optional<Coins> getAssumedLiquidityUpperBound(Pubkey source, Pubkey target) {
        Coins fromMissionControl = getFromMissionControl(source, target).orElse(null);
        Coins fromPayments = getInfo(source, target).getUpperBound().orElse(null);
        if (fromMissionControl == null && fromPayments == null) {
            return Optional.empty();
        }
        if (fromMissionControl == null) {
            return Optional.of(Objects.requireNonNull(fromPayments));
        }
        Coins oneSatBelowFailure = fromMissionControl.subtract(Coins.ofSatoshis(1));
        if (fromPayments == null) {
            return Optional.of(oneSatBelowFailure);
        }
        return Optional.of(oneSatBelowFailure.minimum(fromPayments));
    }

    @Timed
    public Coins getAssumedLiquidityLowerBound(Pubkey source, Pubkey target) {
        return getInfo(source, target).getLowerBound();
    }

    public void markAsMoved(Pubkey source, Pubkey target, Coins amount) {
        getInfo(source, target).move(amount);
    }

    public void markAsAvailable(Pubkey source, Pubkey target, Coins amount) {
        getInfo(source, target).available(amount);
    }

    public void markAsUnavailable(Pubkey source, Pubkey target, Coins amount) {
        getInfo(source, target).unavailable(amount);
    }

    public void markAsInFlight(Pubkey source, Pubkey target, Coins amount) {
        getInfo(source, target).addAsInFlight(amount);
    }

    private LiquidityBounds getInfo(Pubkey source, Pubkey target) {
        return entries.get(new TwoPubkeys(source, target));
    }

    private Optional<Coins> getFromMissionControl(Pubkey source, Pubkey target) {
        boolean useMissionControl = configurationService.getBooleanValue(USE_MISSION_CONTROL).orElse(true);
        if (useMissionControl) {
            return missionControlService.getMinimumOfRecentFailures(source, target);
        }
        return Optional.empty();
    }

    private LiquidityBounds getLiquidityBounds() {
        Duration maxAge = getLiquidityInformationMaxAge().orElse(null);
        if (maxAge == null) {
            return new LiquidityBounds();
        } else {
            return new LiquidityBounds(maxAge);
        }
    }

    private Optional<Duration> getLiquidityInformationMaxAge() {
        return configurationService.getIntegerValue(LIQUIDITY_INFORMATION_MAX_AGE).map(Duration::ofSeconds);
    }

    @SuppressWarnings("UnusedVariable")
    private record TwoPubkeys(Pubkey source, Pubkey target) {
    }
}
