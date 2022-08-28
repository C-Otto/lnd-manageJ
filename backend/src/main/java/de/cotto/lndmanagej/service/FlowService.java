package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdAndMaxAge;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class FlowService {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private static final Duration EXPIRY = Duration.ofMinutes(10);
    private static final Duration REFRESH = Duration.ofMinutes(5);
    private final ForwardingEventsService forwardingEventsService;
    private final ChannelService channelService;
    private final RebalanceService rebalanceService;
    private final SettledInvoicesService settledInvoicesService;
    private final ClosedChannelAwareCache<FlowReport> cache;

    public FlowService(
            ForwardingEventsService forwardingEventsService,
            ChannelService channelService,
            RebalanceService rebalanceService,
            SettledInvoicesService settledInvoicesService,
            OwnNodeService ownNodeService
    ) {
        this.forwardingEventsService = forwardingEventsService;
        this.channelService = channelService;
        this.rebalanceService = rebalanceService;
        this.settledInvoicesService = settledInvoicesService;
        cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH)
                .build(FlowReport.EMPTY, this::getFlowReportForChannelWithoutCache);
    }

    public FlowReport getFlowReportForPeer(Pubkey pubkey) {
        return getFlowReportForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public FlowReport getFlowReportForPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .map(channelId -> getFlowReportForChannel(channelId, maxAge))
                .reduce(FlowReport.EMPTY, FlowReport::add);
    }

    public FlowReport getFlowReportForChannel(ChannelId channelId) {
        return getFlowReportForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public FlowReport getFlowReportForChannel(ChannelId channelId, Duration maxAge) {
        return cache.get(new ChannelIdAndMaxAge(channelId, maxAge));
    }

    private FlowReport getFlowReportForChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        ChannelId channelId = channelIdAndMaxAge.channelId();
        Duration maxAge = channelIdAndMaxAge.maxAge();
        Coins forwardedSent =
                getSumOfAmounts(forwardingEventsService.getEventsWithOutgoingChannel(channelId, maxAge));
        List<ForwardingEvent> incomingEvents = forwardingEventsService.getEventsWithIncomingChannel(channelId, maxAge);
        Coins forwardedReceived = getSumOfAmounts(incomingEvents);
        Coins forwardingFeesReceived = getSumOfFees(incomingEvents);
        Coins rebalanceSent = rebalanceService.getAmountFromChannel(channelId, maxAge);
        Coins rebalanceReceived = rebalanceService.getAmountToChannel(channelId, maxAge);
        Coins rebalanceSupportSent = rebalanceService.getSupportAsSourceAmountFromChannel(channelId, maxAge);
        Coins rebalanceSupportReceived = rebalanceService.getSupportAsTargetAmountToChannel(channelId, maxAge);
        Coins rebalanceFeesSent = rebalanceService.getSourceCostsForChannel(channelId, maxAge);
        Coins rebalanceSupportFeesSent = rebalanceService.getSupportAsSourceCostsFromChannel(channelId, maxAge);
        Coins receivedViaPayments =
                settledInvoicesService.getAmountReceivedViaChannelWithoutSelfPayments(channelId, maxAge);

        return new FlowReport(
                forwardedSent,
                forwardedReceived,
                forwardingFeesReceived,
                rebalanceSent,
                rebalanceFeesSent,
                rebalanceReceived,
                rebalanceSupportSent,
                rebalanceSupportFeesSent,
                rebalanceSupportReceived,
                receivedViaPayments
        );
    }

    private Coins getSumOfAmounts(List<ForwardingEvent> events) {
        return events.stream()
                .map(ForwardingEvent::amountOut)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSumOfFees(List<ForwardingEvent> events) {
        return events.stream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }
}
