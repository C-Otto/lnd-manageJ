package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.privatechannels.PrivateChannelsDao;
import de.cotto.lndmanagej.service.ChannelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PrivateChannelsUpdater {
    private final PrivateChannelsDao dao;
    private final ChannelService channelService;

    public PrivateChannelsUpdater(PrivateChannelsDao dao, ChannelService channelService) {
        this.dao = dao;
        this.channelService = channelService;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void storePrivateFlags() {
        channelService.getOpenChannels().forEach(
                        channel -> dao.savePrivateFlag(channel.getId(), channel.getStatus().privateChannel())
        );
    }
}
