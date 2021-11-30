package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.statistics.ForwardingEventsDao;
import org.springframework.stereotype.Component;

@Component
public class FeeService {
    private final ForwardingEventsDao forwardingEventsDao;
    private final ChannelService channelService;

    public FeeService(ForwardingEventsDao forwardingEventsDao, ChannelService channelService) {
        this.forwardingEventsDao = forwardingEventsDao;
        this.channelService = channelService;
    }

    public FeeReport getFeeReportForPeer(Pubkey pubkey) {
        return new FeeReport(getEarnedFeesForPeer(pubkey), getSourcedFeesForPeer(pubkey));
    }

    public FeeReport getFeeReportForChannel(ChannelId channelId) {
        return new FeeReport(getEarnedFeesForChannel(channelId), getSourcedFeesForChannel(channelId));
    }

    private Coins getEarnedFeesForPeer(Pubkey peer) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(this::getEarnedFeesForChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForPeer(Pubkey peer) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(this::getSourcedFeesForChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getEarnedFeesForChannel(ChannelId channelId) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(channelId).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForChannel(ChannelId channelId) {
        return forwardingEventsDao.getEventsWithIncomingChannel(channelId).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

}
