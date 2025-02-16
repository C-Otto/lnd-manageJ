package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;

@Component
public class OnlinePeersService {
    private static final int DAYS_FOR_ONLINE_PERCENTAGE = 14;
    private static final int DAYS_FOR_CHANGES = 7;
    private static final Duration CACHE_REFRESH = Duration.ofMinutes(1);
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(2);

    private final OnlinePeersDao dao;
    private final LoadingCache<Pubkey, Integer> onlinePercentageCache;
    private final LoadingCache<Pubkey, Integer> changesCache;
    private final OverlappingChannelsService overlappingChannelsService;

    public OnlinePeersService(OnlinePeersDao dao, OverlappingChannelsService overlappingChannelsService) {
        this.dao = dao;
        this.overlappingChannelsService = overlappingChannelsService;
        onlinePercentageCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(this::getOnlinePercentageWithoutCache);
        changesCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(this::getChangesWithoutCache);
    }

    public OnlineReport getOnlineReport(Node node) {
        boolean online = node.online();
        OnlineStatus mostRecentOnlineStatus = dao.getMostRecentOnlineStatus(node.pubkey())
                .orElse(new OnlineStatus(online, now()));
        int onlinePercentage = getOnlinePercentage(node.pubkey());
        int changes = getChanges(node.pubkey());
        if (mostRecentOnlineStatus.online() == online) {
            return OnlineReport.createFromStatus(
                    mostRecentOnlineStatus,
                    onlinePercentage,
                    DAYS_FOR_ONLINE_PERCENTAGE,
                    changes,
                    DAYS_FOR_CHANGES
            );
        }
        return new OnlineReport(
                online,
                now(),
                onlinePercentage,
                DAYS_FOR_ONLINE_PERCENTAGE,
                changes,
                DAYS_FOR_CHANGES
        );
    }

    public int getDaysForOnlinePercentage() {
        return DAYS_FOR_ONLINE_PERCENTAGE;
    }

    public int getOnlinePercentage(Pubkey pubkey) {
        return Objects.requireNonNull(onlinePercentageCache.get(pubkey));
    }

    public int getDaysForChanges() {
        return DAYS_FOR_CHANGES;
    }

    public int getChanges(Pubkey pubkey) {
        return Objects.requireNonNull(changesCache.get(pubkey));
    }

    private int getOnlinePercentageWithoutCache(Pubkey pubkey) {
        Duration total = Duration.ZERO;
        Duration online = Duration.ZERO;
        ZonedDateTime intervalStart = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime cutoff = getCutoff(intervalStart, pubkey);
        for (OnlineStatus onlineStatus : dao.getAllForPeerUpToAgeInDays(pubkey, DAYS_FOR_ONLINE_PERCENTAGE)) {
            ZonedDateTime intervalEnd = onlineStatus.since();
            if (intervalEnd.isBefore(cutoff)) {
                intervalEnd = cutoff;
            }
            Duration difference = Duration.between(intervalEnd, intervalStart);
            total = total.plus(difference);
            if (onlineStatus.online()) {
                online = online.plus(difference);
            }
            intervalStart = intervalEnd;
        }
        if (total.isZero()) {
            return 0;
        }
        return getRoundedPercentage(total, online);
    }

    private ZonedDateTime getCutoff(ZonedDateTime intervalStart, Pubkey pubkey) {
        ZonedDateTime defaultCutoff = intervalStart.minusDays(DAYS_FOR_ONLINE_PERCENTAGE);
        Set<ChannelId> channelsToConsider = overlappingChannelsService.getTransitiveOpenChannels(pubkey);
        if (channelsToConsider.isEmpty()) {
            return defaultCutoff;
        }
        Duration ageOfOldestChannel = overlappingChannelsService.getAgeOfEarliestOpenHeight(channelsToConsider);

        ZonedDateTime cutoffForOldestOpenChannel = intervalStart.minus(ageOfOldestChannel);
        if (defaultCutoff.isBefore(cutoffForOldestOpenChannel)) {
            return cutoffForOldestOpenChannel;
        }
        return defaultCutoff;
    }

    private int getRoundedPercentage(Duration total, Duration offline) {
        return (int) (offline.toSeconds() * 100.0 / total.toSeconds());
    }

    private int getChangesWithoutCache(Pubkey pubkey) {
        Boolean lastKnownStatus = null;
        int changes = -1;
        for (OnlineStatus onlineStatus : dao.getAllForPeerUpToAgeInDays(pubkey, DAYS_FOR_CHANGES)) {
            boolean status = onlineStatus.online();
            if (lastKnownStatus == null || status != lastKnownStatus) {
                changes++;
                lastKnownStatus = status;
            }
        }
        return Math.max(0, changes);
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }
}
