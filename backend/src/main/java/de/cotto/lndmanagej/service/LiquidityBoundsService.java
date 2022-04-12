package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LiquidityBounds;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class LiquidityBoundsService {
    private final MissionControlService missionControlService;
    private final LoadingCache<TwoPubkeys, LiquidityBounds> entries;

    public LiquidityBoundsService(MissionControlService missionControlService) {
        this.missionControlService = missionControlService;
        entries = new CacheBuilder()
                .withSoftValues(true)
                .build(ignored -> new LiquidityBounds());
    }

    @Timed
    public Optional<Coins> getAssumedLiquidityUpperBound(Pubkey source, Pubkey target) {
        Coins fromMissionControl = missionControlService.getMinimumOfRecentFailures(source, target).orElse(null);
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

    private LiquidityBounds getInfo(Pubkey source, Pubkey target) {
        return entries.get(new TwoPubkeys(source, target));
    }

    @SuppressWarnings("UnusedVariable")
    private record TwoPubkeys(Pubkey source, Pubkey target) {
    }
}
