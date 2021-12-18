package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@Component
public class OnlinePeersUpdater {
    private final OnlinePeersDao dao;
    private final ChannelService channelService;
    private final NodeService nodeService;

    public OnlinePeersUpdater(OnlinePeersDao dao, ChannelService channelService, NodeService nodeService) {
        this.dao = dao;
        this.channelService = channelService;
        this.nodeService = nodeService;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void storePeerOnlineStatus() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        channelService.getOpenChannels().stream()
                .map(LocalChannel::getRemotePubkey)
                .distinct()
                .map(nodeService::getNode)
                .filter(node -> dao.getMostRecentOnlineStatus(node.pubkey()).orElse(!node.online()) != node.online())
                .forEach(node -> dao.saveOnlineStatus(node.pubkey(), node.online(), now));
    }
}
