package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class FeeService {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private final ForwardingEventsService forwardingEventsService;
    private final ChannelService channelService;

    public FeeService(
            ForwardingEventsService forwardingEventsService,
            ChannelService channelService
    ) {
        this.forwardingEventsService = forwardingEventsService;
        this.channelService = channelService;
    }

    public FeeReport getFeeReportForPeer(Pubkey pubkey) {
        return getFeeReportForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public FeeReport getFeeReportForPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getFeeReportForChannel(channelId, maxAge))
                .reduce(FeeReport.EMPTY, FeeReport::add);
    }

    public FeeReport getFeeReportForChannel(ChannelId channelId) {
        return getFeeReportForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public FeeReport getFeeReportForChannel(ChannelId channelId, Duration maxAge) {
        return new FeeReport(
                getSumOfFees(forwardingEventsService.getEventsWithOutgoingChannel(channelId, maxAge)),
                getSumOfFees(forwardingEventsService.getEventsWithIncomingChannel(channelId, maxAge))
        );
    }

    private Coins getSumOfFees(List<ForwardingEvent> events) {
        return events.parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }
}
