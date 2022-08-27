package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    @InjectMocks
    private FlowService flowService;

    @Mock
    private ChannelService channelService;

    @Mock
    private ForwardingEventsService forwardingEventsService;

    @Mock
    private RebalanceService rebalanceService;

    @Mock
    private SettledInvoicesService settledInvoicesService;

    @BeforeEach
    void setUp() {
        lenient().when(rebalanceService.getAmountFromChannel(any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getAmountFromChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getAmountToChannel(any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getAmountToChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSupportAsSourceAmountFromChannel(any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSupportAsSourceAmountFromChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSupportAsTargetAmountToChannel(any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSupportAsTargetAmountToChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSourceCostsForChannel(any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSourceCostsForChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getSupportAsSourceCostsFromChannel(any(), any())).thenReturn(Coins.NONE);
        lenient().when(settledInvoicesService.getAmountReceivedViaChannel(any(), any())).thenReturn(Coins.NONE);
    }

    @Test
    void getFlowReportForPeer_no_channel() {
        assertThat(flowService.getFlowReportForPeer(PUBKEY)).isEqualTo(FlowReport.EMPTY);
    }

    @Test
    void getFlowReportForPeer_default_max_age() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        mockSent(DEFAULT_MAX_AGE, CHANNEL_ID, 1_000L);
        mockSent(DEFAULT_MAX_AGE, CHANNEL_ID_2, 50L);
        mockReceived(DEFAULT_MAX_AGE, CHANNEL_ID, 9_000L);
        mockReceived(DEFAULT_MAX_AGE, CHANNEL_ID_2, 1L);
        mockRebalanceFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 1, 2);
        mockRebalanceFromTo(DEFAULT_MAX_AGE, CHANNEL_ID_2, 3, 4);
        mockRebalanceSupportFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 555, 6);
        mockRebalanceSupportFromTo(DEFAULT_MAX_AGE, CHANNEL_ID_2, 7, 888);
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(Coins.ofMilliSatoshis(1_500_000));
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE))
                .thenReturn(Coins.ofMilliSatoshis(915_000));
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(1_000 + 50),
                Coins.ofSatoshis(9_000 + 1),
                Coins.ofMilliSatoshis(9_000 + 1),
                Coins.ofSatoshis(1 + 3),
                Coins.ofMilliSatoshis(1 + 3),
                Coins.ofSatoshis(2 + 4),
                Coins.ofSatoshis(555 + 7),
                Coins.ofMilliSatoshis(555 + 7),
                Coins.ofSatoshis(6 + 888),
                Coins.ofMilliSatoshis(1_500_000 + 915_000 - 2_000 - 4_000 - 6_000 - 888_000)
        );
        assertThat(flowService.getFlowReportForPeer(PUBKEY)).isEqualTo(flowReport);
        verify(forwardingEventsService).getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_AGE);
        verify(forwardingEventsService).getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_AGE);
        verify(forwardingEventsService).getEventsWithOutgoingChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE);
        verify(forwardingEventsService).getEventsWithIncomingChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE);
    }

    @Test
    void getFlowReportForPeer() {
        Duration maxAge = Duration.ofDays(14);
        mockSent(maxAge, CHANNEL_ID, 1_000L, 50L);
        mockSent(maxAge, CHANNEL_ID_2, 1L);
        mockReceived(maxAge, CHANNEL_ID, 1_000L, 8_001L);
        mockReceived(maxAge, CHANNEL_ID_2, 2L);
        mockRebalanceFromTo(maxAge, CHANNEL_ID, 111, 2);
        mockRebalanceFromTo(maxAge, CHANNEL_ID_2, 333, 4);
        mockRebalanceSupportFromTo(maxAge, CHANNEL_ID, 5, 6);
        mockRebalanceSupportFromTo(maxAge, CHANNEL_ID_2, 7, 8);
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, maxAge))
                .thenReturn(Coins.ofMilliSatoshis(1_500_000));
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID_2, maxAge))
                .thenReturn(Coins.ofMilliSatoshis(15_000));
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(1_000 + 50 + 1),
                Coins.ofSatoshis(1_000 + 8_001 + 2),
                Coins.ofMilliSatoshis(1_000 + 8_001 + 2),
                Coins.ofSatoshis(111 + 333),
                Coins.ofMilliSatoshis(111 + 333),
                Coins.ofSatoshis(2 + 4),
                Coins.ofSatoshis(5 + 7),
                Coins.ofMilliSatoshis(5 + 7),
                Coins.ofSatoshis(6 + 8),
                Coins.ofMilliSatoshis(1_500_000 + 15_000 - 2_000 - 4_000 - 6_000 - 8_000)
        );
        assertThat(flowService.getFlowReportForPeer(PUBKEY, maxAge)).isEqualTo(flowReport);
    }

    @Test
    void getFlowReportForChannel_default_max_age() {
        mockSent(DEFAULT_MAX_AGE, CHANNEL_ID, 1_050L);
        mockReceived(DEFAULT_MAX_AGE, CHANNEL_ID, 9_001L);
        mockRebalanceFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 100, 200);
        mockRebalanceSupportFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 5, 6);
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(Coins.ofMilliSatoshis(1_500_000));
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(1_050),
                Coins.ofSatoshis(9_001),
                Coins.ofMilliSatoshis(9_001),
                Coins.ofSatoshis(100),
                Coins.ofMilliSatoshis(100),
                Coins.ofSatoshis(200),
                Coins.ofSatoshis(5),
                Coins.ofMilliSatoshis(5),
                Coins.ofSatoshis(6),
                Coins.ofMilliSatoshis(1_500_000 - 200_000 - 6_000)
        );
        assertThat(flowService.getFlowReportForChannel(CHANNEL_ID)).isEqualTo(flowReport);
        verify(forwardingEventsService).getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_AGE);
        verify(forwardingEventsService).getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_AGE);
    }

    @Test
    void getFlowReportForChannel() {
        Duration maxAge = Duration.ofDays(14);
        mockSent(maxAge, CHANNEL_ID, 1_000L, 50L);
        mockReceived(maxAge, CHANNEL_ID, 1_000L, 8_001L);
        mockRebalanceFromTo(maxAge, CHANNEL_ID, 101, 201);
        mockRebalanceSupportFromTo(maxAge, CHANNEL_ID, 5, 6);
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, maxAge))
                .thenReturn(Coins.ofMilliSatoshis(1_500_000));
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(1_000 + 50),
                Coins.ofSatoshis(1_000 + 8_001),
                Coins.ofMilliSatoshis(1_000 + 8_001),
                Coins.ofSatoshis(101),
                Coins.ofMilliSatoshis(101),
                Coins.ofSatoshis(201),
                Coins.ofSatoshis(5),
                Coins.ofMilliSatoshis(5),
                Coins.ofSatoshis(6),
                Coins.ofMilliSatoshis(1_500_000 - 201_000 - 6_000)
        );
        assertThat(flowService.getFlowReportForChannel(CHANNEL_ID, maxAge)).isEqualTo(flowReport);
    }

    @Test
    void getFlowReportForChannel_includes_receivedViaPayments() {
        Coins expectedReceivedViaPayments = Coins.ofMilliSatoshis(1);
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(expectedReceivedViaPayments);
        assertThat(flowService.getFlowReportForChannel(CHANNEL_ID).receivedViaPayments())
                .isEqualTo(expectedReceivedViaPayments);
    }

    @Test
    void getFlowReportForChannel_nothing_received_via_settled_invoices_but_only_via_rebalances() {
        Coins expectedReceivedViaPayments = Coins.NONE;
        Coins expectedRebalanceReceived = Coins.ofSatoshis(1);
        mockRebalanceFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 0, expectedRebalanceReceived.satoshis());
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(expectedReceivedViaPayments);
        assertThat(flowService.getFlowReportForChannel(CHANNEL_ID).receivedViaPayments())
                .isEqualTo(expectedReceivedViaPayments);
    }

    @Test
    void getFlowReportForChannel_self_payments_do_not_count_for_receivedViaPayments() {
        Coins expectedReceivedViaPayments = Coins.ofSatoshis(3);
        Coins expectedRebalanceReceived = Coins.ofSatoshis(1);

        mockRebalanceFromTo(DEFAULT_MAX_AGE, CHANNEL_ID, 0, expectedRebalanceReceived.satoshis());
        when(settledInvoicesService.getAmountReceivedViaChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(expectedReceivedViaPayments.add(expectedRebalanceReceived));
        assertThat(flowService.getFlowReportForChannel(CHANNEL_ID).receivedViaPayments())
                .isEqualTo(expectedReceivedViaPayments);
    }

    private void mockReceived(Duration maxAge, ChannelId channelId, Long... amounts) {
        List<ForwardingEvent> events = createListOfEvents(channelId, CHANNEL_ID_2, amounts);
        when(forwardingEventsService.getEventsWithIncomingChannel(channelId, maxAge)).thenReturn(events);
    }

    private void mockSent(Duration maxAge, ChannelId channelId, Long... amounts) {
        List<ForwardingEvent> events = createListOfEvents(CHANNEL_ID_2, channelId, amounts);
        when(forwardingEventsService.getEventsWithOutgoingChannel(channelId, maxAge)).thenReturn(events);
    }

    private void mockRebalanceFromTo(Duration maxAge, ChannelId channelId, long satoshisFrom, long satoshisTo) {
        when(rebalanceService.getAmountFromChannel(channelId, maxAge)).thenReturn(Coins.ofSatoshis(satoshisFrom));
        when(rebalanceService.getAmountToChannel(channelId, maxAge)).thenReturn(Coins.ofSatoshis(satoshisTo));
        when(rebalanceService.getSourceCostsForChannel(channelId, maxAge))
                .thenReturn(Coins.ofMilliSatoshis(satoshisFrom));
    }

    private void mockRebalanceSupportFromTo(Duration maxAge, ChannelId channelId, long satoshisFrom, long satoshisTo) {
        when(rebalanceService.getSupportAsSourceAmountFromChannel(channelId, maxAge))
                .thenReturn(Coins.ofSatoshis(satoshisFrom));
        when(rebalanceService.getSupportAsTargetAmountToChannel(channelId, maxAge))
                .thenReturn(Coins.ofSatoshis(satoshisTo));
        when(rebalanceService.getSupportAsSourceCostsFromChannel(channelId, maxAge))
                .thenReturn(Coins.ofMilliSatoshis(satoshisFrom));
    }

    private List<ForwardingEvent> createListOfEvents(ChannelId channelIn, ChannelId channelOut, Long... amounts) {
        LocalDateTime timestamp = FORWARDING_EVENT.timestamp();
        return Arrays.stream(amounts)
                .map(Coins::ofSatoshis)
                .map(amount -> new ForwardingEvent(
                        0,
                        amount.add(Coins.ofMilliSatoshis(amount.milliSatoshis() / 1000)),
                        amount,
                        channelIn,
                        channelOut,
                        timestamp
                ))
                .toList();
    }
}
