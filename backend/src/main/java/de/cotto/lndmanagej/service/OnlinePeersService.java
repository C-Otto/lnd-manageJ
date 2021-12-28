package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
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

@Component
public class OnlinePeersService {
    private static final int DAYS_FOR_OFFLINE_PERCENTAGE = 7;
    private static final int DAYS_FOR_CHANGES = 7;
    private static final Duration CACHE_REFRESH = Duration.ofMinutes(1);
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(2);

    private final OnlinePeersDao dao;
    private final LoadingCache<Pubkey, Integer> onlinePercentageCache;
    private final LoadingCache<Pubkey, Integer> changesCache;

    public OnlinePeersService(OnlinePeersDao dao) {
        this.dao = dao;
        onlinePercentageCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(this::getOnlinePercentageLastWeekWithoutCache);
        changesCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(this::getChangesLastWeekWithoutCache);
    }

    public OnlineReport getOnlineReport(Node node) {
        boolean online = node.online();
        OnlineStatus mostRecentOnlineStatus = dao.getMostRecentOnlineStatus(node.pubkey())
                .orElse(new OnlineStatus(online, now()));
        int onlinePercentageLastWeek = getOnlinePercentageLastWeek(node.pubkey());
        int changesLastWeek = getChangesLastWeek(node.pubkey());
        if (mostRecentOnlineStatus.online() == online) {
            return OnlineReport.createFromStatus(mostRecentOnlineStatus, onlinePercentageLastWeek, changesLastWeek);
        }
        return new OnlineReport(online, now(), onlinePercentageLastWeek, changesLastWeek);
    }

    public int getOnlinePercentageLastWeek(Pubkey pubkey) {
        return Objects.requireNonNull(onlinePercentageCache.get(pubkey));
    }

    public int getChangesLastWeek(Pubkey pubkey) {
        return Objects.requireNonNull(changesCache.get(pubkey));
    }

    private int getOnlinePercentageLastWeekWithoutCache(Pubkey pubkey) {
        Duration total = Duration.ZERO;
        Duration online = Duration.ZERO;
        ZonedDateTime intervalStart = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime cutoff = intervalStart.minusDays(DAYS_FOR_OFFLINE_PERCENTAGE);
        for (OnlineStatus onlineStatus : dao.getAllForPeerUpToAgeInDays(pubkey, DAYS_FOR_OFFLINE_PERCENTAGE)) {
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

    private int getRoundedPercentage(Duration total, Duration offline) {
        return (int) (offline.getSeconds() * 100.0 / total.getSeconds());
    }

    private int getChangesLastWeekWithoutCache(Pubkey pubkey) {
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
