package de.cotto.lndmanagej.service;

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
import java.util.List;

@Component
public class OnlinePeersService {
    private static final int DAYS_FOR_OFFLINE_PERCENTAGE = 7;
    private final OnlinePeersDao dao;

    public OnlinePeersService(OnlinePeersDao dao) {
        this.dao = dao;
    }

    public OnlineReport getOnlineReport(Node node) {
        boolean online = node.online();
        OnlineStatus mostRecentOnlineStatus = dao.getMostRecentOnlineStatus(node.pubkey())
                .orElse(new OnlineStatus(online, now()));
        int onlinePercentageLastWeek = getOnlinePercentageLastWeek(node.pubkey());
        if (mostRecentOnlineStatus.online() == online) {
            return OnlineReport.createFromStatus(mostRecentOnlineStatus, onlinePercentageLastWeek);
        }
        return new OnlineReport(online, now(), onlinePercentageLastWeek);
    }

    public int getOnlinePercentageLastWeek(Pubkey pubkey) {
        Duration total = Duration.ZERO;
        Duration online = Duration.ZERO;
        ZonedDateTime intervalStart = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime cutoff = intervalStart.minusDays(DAYS_FOR_OFFLINE_PERCENTAGE);
        boolean shouldContinue = true;
        List<OnlineStatus> allForPeer = dao.getAllForPeer(pubkey);
        for (int i = 0; i < allForPeer.size() && shouldContinue; i++) {
            OnlineStatus onlineStatus = allForPeer.get(i);
            ZonedDateTime intervalEnd = onlineStatus.since();
            if (intervalEnd.isBefore(cutoff)) {
                intervalEnd = cutoff;
                shouldContinue = false;
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

    private ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }
}
