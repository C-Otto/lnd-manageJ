package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcMissionControl;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.MissionControlEntry;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class MissionControlService {
    private static final Duration MAX_AGE_TO_CONSIDER = Duration.ofHours(1);

    private final GrpcMissionControl grpcMissionControl;
    private final LoadingCache<Object, Optional<Map<Pubkey, Map<Pubkey, Coins>>>> cachedFailures;

    public MissionControlService(GrpcMissionControl grpcMissionControl) {
        this.grpcMissionControl = grpcMissionControl;
        cachedFailures = new CacheBuilder()
                .withExpiry(Duration.ofSeconds(10))
                .withRefresh(Duration.ofSeconds(5))
                .withSoftValues(true)
                .build(this::populateFailureMapFromMissionControl);
    }

    public Optional<Coins> getMinimumOfRecentFailures(Pubkey source, Pubkey target) {
        Map<Pubkey, Map<Pubkey, Coins>> map = Objects.requireNonNull(cachedFailures.get("")).orElse(null);
        if (map == null) {
            return Optional.empty();
        }
        Map<Pubkey, Coins> innerMap = map.getOrDefault(source, Map.of());
        return Optional.ofNullable(innerMap.get(target));
    }

    private Optional<Map<Pubkey, Map<Pubkey, Coins>>> populateFailureMapFromMissionControl() {
        Set<MissionControlEntry> missionControlEntries = grpcMissionControl.getEntries().orElse(null);
        if (missionControlEntries == null) {
            return Optional.empty();
        }
        Instant threshold = Instant.now().minus(MAX_AGE_TO_CONSIDER);
        Map<Pubkey, Map<Pubkey, Coins>> failureMap = new LinkedHashMap<>();
        for (MissionControlEntry entry : missionControlEntries) {
            if (entry.success()) {
                continue;
            }
            if (entry.isAfter(threshold)) {
                setMinimum(failureMap, entry.source(), entry.target(), entry.amount());
            }
        }
        return Optional.of(failureMap);
    }

    private void setMinimum(Map<Pubkey, Map<Pubkey, Coins>> map, Pubkey source, Pubkey target, Coins amount) {
        Map<Pubkey, Coins> innerMap = map.compute(source, (k, v) -> v == null ? new LinkedHashMap<>() : v);
        innerMap.compute(target, (k, v) -> amount.minimum(v));
    }
}
