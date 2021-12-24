package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class OnlinePeersService {
    private final OnlinePeersDao dao;

    public OnlinePeersService(OnlinePeersDao dao) {
        this.dao = dao;
    }

    public OnlineReport getOnlineReport(Node node) {
        boolean online = node.online();
        OnlineStatus mostRecentOnlineStatus = dao.getMostRecentOnlineStatus(node.pubkey()).orElse(null);
        if (mostRecentOnlineStatus != null && mostRecentOnlineStatus.online() == online) {
            return OnlineReport.createFromStatus(mostRecentOnlineStatus);
        }
        return new OnlineReport(online, now());
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }
}
