package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import de.cotto.lndmanagej.model.RebalanceReport;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_CREATION_DATE_TIME;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_FEES;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_HASH;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_VALUE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.ADD_INDEX;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.AMOUNT_PAID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.HASH;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLE_DATE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLE_INDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebalanceServiceTest {
    private static final Coins FEE_FOR_TWO_REBALANCES = Coins.ofMilliSatoshis(20);
    private static final Coins AMOUNT_FOR_TWO_REBALANCES = Coins.ofMilliSatoshis(246);

    @InjectMocks
    private RebalanceService rebalanceService;

    @Mock
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private ChannelService channelService;

    @Test
    void getRebalanceReportForChannel_empty() {
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID)).isEqualTo(RebalanceReport.EMPTY);
    }

    @Test
    void getRebalanceReportForChannel_source() {
        RebalanceReport expected =
                new RebalanceReport(FEE_FOR_TWO_REBALANCES, AMOUNT_FOR_TWO_REBALANCES, Coins.NONE, Coins.NONE);
        mockSelfPaymentsFromChannel("from " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getRebalanceReportForChannel_target() {
        RebalanceReport expected =
                new RebalanceReport(Coins.NONE, Coins.NONE, FEE_FOR_TWO_REBALANCES, AMOUNT_FOR_TWO_REBALANCES);
        mockSelfPaymentsToChannel("to " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID_2)).isEqualTo(expected);
    }

    @Test
    void getRebalanceReportForPeer_source() {
        RebalanceReport expected =
                new RebalanceReport(FEE_FOR_TWO_REBALANCES, AMOUNT_FOR_TWO_REBALANCES, Coins.NONE, Coins.NONE);
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getRebalanceReportForPeer_target() {
        RebalanceReport expected =
                new RebalanceReport(Coins.NONE, Coins.NONE, FEE_FOR_TWO_REBALANCES, AMOUNT_FOR_TWO_REBALANCES);
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getSourceCostsForChannel() {
        mockSelfPaymentsFromChannel("from " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel() {
        mockSelfPaymentsToChannel("to " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForPeer() {
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getSourceCostsForPeer(PUBKEY)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForPeer() {
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getTargetCostsForPeer(PUBKEY)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountFromPeer() {
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getAmountFromPeer(PUBKEY)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountToPeer() {
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getAmountToPeer(PUBKEY)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountFromChannel() {
        mockSelfPaymentsFromChannel("from " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getAmountFromChannel(CHANNEL_ID)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountToChannel() {
        mockSelfPaymentsToChannel("to " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getAmountToChannel(CHANNEL_ID_2)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForChannel_short_channel_id_as_infix_in_memo() {
        mockSelfPaymentsFromChannel("111" + CHANNEL_ID.getShortChannelId() + "222");
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactForm());
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactFormLnd());
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForChannel_id_not_in_memo() {
        mockSelfPaymentsFromChannel("something");
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getTargetCostsForChannel_no_self_payments() {
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(Coins.NONE);
    }

    @Test
    void getTargetCostsForChannel_short_channel_id_in_memo() {
        mockSelfPaymentsToChannel("foo bar " + CHANNEL_ID_2.getShortChannelId() + "!");
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsToChannel("111" + CHANNEL_ID_2.getCompactForm() + "222");
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsToChannel(CHANNEL_ID_2.getCompactFormLnd());
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_id_not_in_memo() {
        mockSelfPaymentsToChannel("something");
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_other_channel_id_in_memo() {
        mockSelfPaymentsToChannel("rebalance to " + CHANNEL_ID_3);
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_source_channel_id_in_memo() {
        mockSelfPaymentsToChannel("something: " + CHANNEL_ID);
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(Coins.NONE);
    }

    @Test
    void getTargetCostsForChannel_source_channel_not_known() {
        List<PaymentRoute> noRoute = List.of(new PaymentRoute(List.of()));
        Payment payment = new Payment(
                PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, noRoute
        );
        SettledInvoice settledInvoice = getSettledInvoice("something", 0);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2))
                .thenReturn(List.of(new SelfPayment(payment, settledInvoice)));
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(PAYMENT_FEES);
    }

    private void mockSelfPaymentsFromChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(
                getSelfPayment(memo, 0),
                getSelfPayment(memo, 1)
        ));
    }

    private void mockSelfPaymentsToChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2)).thenReturn(List.of(
                getSelfPayment(memo, 0),
                getSelfPayment(memo, 1)
        ));
    }

    private SelfPayment getSelfPayment(String memo, int offset) {
        List<PaymentRoute> routes = getSingleRoute();
        Payment payment = new Payment(
                PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, routes
        );
        SettledInvoice settledInvoice = getSettledInvoice(memo, offset);
        return new SelfPayment(payment, settledInvoice);
    }

    private SettledInvoice getSettledInvoice(String memo, int offset) {
        return new SettledInvoice(
                ADD_INDEX,
                SETTLE_INDEX,
                SETTLE_DATE.plusSeconds(offset),
                HASH,
                AMOUNT_PAID,
                memo,
                Optional.empty(),
                Optional.of(CHANNEL_ID_2)
        );
    }

    private List<PaymentRoute> getSingleRoute() {
        PaymentHop firstHop = new PaymentHop(CHANNEL_ID, Coins.NONE);
        PaymentHop lastHop = new PaymentHop(CHANNEL_ID_2, Coins.NONE);
        PaymentRoute route = new PaymentRoute(List.of(firstHop, lastHop));
        return List.of(route);
    }

    private void mockTwoChannelsAndPaymentsToPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL_3.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_3, CLOSED_CHANNEL_2));
        SelfPayment selfPayment1 = getSelfPayment(id1.toString(), 0);
        SelfPayment selfPayment2 = getSelfPayment(id2.toString(), 1);
        when(selfPaymentsService.getSelfPaymentsToChannel(id1)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsToChannel(id2)).thenReturn(List.of(selfPayment2));
    }

    private void mockTwoChannelsAndPaymentsFromPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        SelfPayment selfPayment1 = getSelfPayment(id1.toString(), 0);
        SelfPayment selfPayment2 = getSelfPayment(id2.toString(), 1);
        when(selfPaymentsService.getSelfPaymentsFromChannel(id1)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsFromChannel(id2)).thenReturn(List.of(selfPayment2));
    }
}