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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
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
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private static final Coins FEE_FOR_TWO_REBALANCES = Coins.ofMilliSatoshis(20);
    private static final Coins AMOUNT_FOR_TWO_REBALANCES = Coins.ofMilliSatoshis(246);

    @InjectMocks
    private RebalanceService rebalanceService;

    @Mock
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private ChannelService channelService;

    @Test
    void getReportForChannel_empty() {
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID)).isEqualTo(RebalanceReport.EMPTY);
    }

    @Test
    void getReportForChannel_source() {
        RebalanceReport expected = new RebalanceReport(
                FEE_FOR_TWO_REBALANCES,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE
        );
        mockSelfPaymentsFromChannel("source: " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getReportForChannel_target() {
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                FEE_FOR_TWO_REBALANCES,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE,
                Coins.NONE
        );
        mockSelfPaymentsToChannel("sending to " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID_2)).isEqualTo(expected);
    }

    @Test
    void getReportForChannel_supportAsSource() {
        RebalanceReport expected = new RebalanceReport(
                PAYMENT_FEES,
                AMOUNT_PAID,
                Coins.NONE,
                Coins.NONE,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE
        );
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("dest: " + CHANNEL_ID_2, 0),
                getSelfPayment("into " + CHANNEL_ID_2, 1),
                getSelfPayment(CHANNEL_ID.toString(), 1)
        ));
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getReportForChannel_supportAsTarget() {
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                PAYMENT_FEES,
                AMOUNT_PAID,
                Coins.NONE,
                AMOUNT_FOR_TWO_REBALANCES
        );
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("filling up " + CHANNEL_ID_2, 0),
                getSelfPayment("f: " + CHANNEL_ID, 1),
                getSelfPayment(CHANNEL_ID + " is source", 1)
        ));
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID_2)).isEqualTo(expected);
    }

    @Test
    void getReportForChannel_with_max_age() {
        Duration maxAge = Duration.between(PAYMENT_CREATION_DATE_TIME, LocalDateTime.now(ZoneOffset.UTC));
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                AMOUNT_PAID
        );
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment(String.valueOf(CHANNEL_ID), 0)
        ));
        assertThat(rebalanceService.getReportForChannel(CHANNEL_ID_2, maxAge)).isEqualTo(expected);
    }

    @Test
    void getReportForPeer_source() {
        RebalanceReport expected = new RebalanceReport(
                FEE_FOR_TWO_REBALANCES,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE
        );
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getReportForPeer_target() {
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                FEE_FOR_TWO_REBALANCES,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE,
                Coins.NONE
        );
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getReportForPeer_supportAsSource() {
        RebalanceReport expected = new RebalanceReport(
                FEE_FOR_TWO_REBALANCES,
                AMOUNT_FOR_TWO_REBALANCES,
                Coins.NONE,
                Coins.NONE,
                Coins.ofMilliSatoshis(123),
                Coins.NONE
        );
        mockSupportAsSourceForPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getReportForPeer_supportAsTarget() {
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                Coins.ofMilliSatoshis(20),
                Coins.ofMilliSatoshis(246),
                Coins.NONE,
                Coins.ofMilliSatoshis(123)
        );
        mockSupportAsTargetForPeer();
        assertThat(rebalanceService.getReportForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getReportForPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(2);
        RebalanceReport expected = new RebalanceReport(
                Coins.NONE,
                Coins.NONE,
                Coins.ofMilliSatoshis(10),
                Coins.ofMilliSatoshis(123),
                Coins.NONE,
                Coins.NONE
        );
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2));
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment("x", 2)
        ));
        assertThat(rebalanceService.getReportForPeer(PUBKEY, maxAge)).isEqualTo(expected);
    }

    @Test
    void getSourceCostsForChannel() {
        mockSelfPaymentsFromChannel(CHANNEL_ID.toString());
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(3);
        String memo = CHANNEL_ID.toString();
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(
                getSelfPayment(memo, 0)
        ));
        assertThat(rebalanceService.getSourceCostsForChannel(CHANNEL_ID, maxAge)).isEqualTo(PAYMENT_FEES);
    }

    @Test
    void getTargetCostsForChannel() {
        mockSelfPaymentsToChannel("sending into " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(4);
        String memo = "sending into " + CHANNEL_ID_2.getShortChannelId();
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment(memo, 0),
                getSelfPayment(memo, 1)
        ));
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2, maxAge)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForPeer() {
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getSourceCostsForPeer(PUBKEY)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getSourceCostsForPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(5);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        SelfPayment selfPayment = getSelfPayment(CHANNEL_ID.toString(), 0);
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(selfPayment));
        assertThat(rebalanceService.getSourceCostsForPeer(PUBKEY, maxAge)).isEqualTo(PAYMENT_FEES);
    }

    @Test
    void getTargetCostsForPeer() {
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getTargetCostsForPeer(PUBKEY)).isEqualTo(FEE_FOR_TWO_REBALANCES);
    }

    @Test
    void getTargetCostsForPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(6);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_3));
        SelfPayment selfPayment = getSelfPayment(CHANNEL_ID_3.toString(), 0);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_3, maxAge)).thenReturn(List.of(selfPayment));
        assertThat(rebalanceService.getTargetCostsForPeer(PUBKEY, maxAge)).isEqualTo(PAYMENT_FEES);
    }

    @Test
    void getAmountFromPeer() {
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getAmountFromPeer(PUBKEY)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountFromPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(7);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2));
        SelfPayment selfPayment = getSelfPayment(CHANNEL_ID_2.toString(), 0);
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(selfPayment));
        assertThat(rebalanceService.getAmountFromPeer(PUBKEY, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getAmountToPeer() {
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getAmountToPeer(PUBKEY)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountToPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(8);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_2));
        SelfPayment selfPayment = getSelfPayment(CHANNEL_ID_2.toString(), 0);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(selfPayment));
        assertThat(rebalanceService.getAmountToPeer(PUBKEY, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getAmountFromChannel() {
        mockSelfPaymentsFromChannel("taking out of " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getAmountFromChannel(CHANNEL_ID)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountFromChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(9);
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(
                getSelfPayment("<" + CHANNEL_ID.getShortChannelId(), 0)
        ));
        assertThat(rebalanceService.getAmountFromChannel(CHANNEL_ID, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getAmountToChannel() {
        mockSelfPaymentsToChannel("into " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getAmountToChannel(CHANNEL_ID_2)).isEqualTo(AMOUNT_FOR_TWO_REBALANCES);
    }

    @Test
    void getAmountToChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(10);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment("memo", 0)
        ));
        assertThat(rebalanceService.getAmountToChannel(CHANNEL_ID_2, maxAge)).isEqualTo(AMOUNT_PAID);
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
    void getSupportAsSourceAmountFromChannel() {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("out of " + CHANNEL_ID, 0),
                getSelfPayment(String.valueOf(CHANNEL_ID), 1),
                getSelfPayment("rebalancing " + CHANNEL_ID_2, 2)
        ));
        assertThat(rebalanceService.getSupportAsSourceAmountFromChannel(CHANNEL_ID)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsSourceAmountFromChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(11);
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(
                getSelfPayment("out of " + CHANNEL_ID, 0),
                getSelfPayment("emptying " + CHANNEL_ID, 1),
                getSelfPayment("rebalancing " + CHANNEL_ID_2, 2)
        ));
        assertThat(rebalanceService.getSupportAsSourceAmountFromChannel(CHANNEL_ID, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsSourceCostsFromChannel() {
        Duration maxAge = Duration.ofDays(11);
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(
                getSelfPayment(String.valueOf(CHANNEL_ID_2), 2)
        ));
        assertThat(rebalanceService.getSupportAsSourceCostsFromChannel(CHANNEL_ID, maxAge)).isEqualTo(PAYMENT_FEES);
    }

    @Test
    void getSupportAsSourceAmountFromPeer() {
        mockSupportAsSourceForPeer();
        assertThat(rebalanceService.getSupportAsSourceAmountFromPeer(PUBKEY)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsSourceAmountFromPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(12);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL));
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(
                getSelfPayment("into some other channel", 0)
        ));
        assertThat(rebalanceService.getSupportAsSourceAmountFromPeer(PUBKEY, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsTargetAmountToChannel() {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("<<< " + CHANNEL_ID, 0),
                getSelfPayment("into " + CHANNEL_ID_2, 1),
                getSelfPayment("to " + CHANNEL_ID_2, 2)
        ));
        assertThat(rebalanceService.getSupportAsTargetAmountToChannel(CHANNEL_ID_2)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsTargetAmountToChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(13);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment("emptying " + CHANNEL_ID, 0)
        ));
        assertThat(rebalanceService.getSupportAsTargetAmountToChannel(CHANNEL_ID_2, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsTargetAmountToPeer() {
        mockSupportAsTargetForPeer();
        assertThat(rebalanceService.getSupportAsTargetAmountToPeer(PUBKEY)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getSupportAsTargetAmountToPeer_with_max_age() {
        Duration maxAge = Duration.ofDays(14);
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2));
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, maxAge)).thenReturn(List.of(
                getSelfPayment("from " + CHANNEL_ID, 0)
        ));
        assertThat(rebalanceService.getSupportAsTargetAmountToPeer(PUBKEY, maxAge)).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void getTargetCostsForChannel_source_channel_not_known() {
        List<PaymentRoute> noRoute = List.of(new PaymentRoute(List.of()));
        Payment payment = new Payment(
                PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, noRoute
        );
        SettledInvoice settledInvoice = getSettledInvoice("something", 0);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE))
                .thenReturn(List.of(new SelfPayment(payment, settledInvoice)));
        assertThat(rebalanceService.getTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(PAYMENT_FEES);
    }

    private void mockSelfPaymentsFromChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment(memo, 0),
                getSelfPayment(memo, 1)
        ));
    }

    private void mockSelfPaymentsToChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE)).thenReturn(List.of(
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
        when(selfPaymentsService.getSelfPaymentsToChannel(id1, DEFAULT_MAX_AGE)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsToChannel(id2, DEFAULT_MAX_AGE)).thenReturn(List.of(selfPayment2));
    }

    private void mockTwoChannelsAndPaymentsFromPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        SelfPayment selfPayment1 = getSelfPayment(id1.toString(), 0);
        SelfPayment selfPayment2 = getSelfPayment(id2.toString(), 1);
        when(selfPaymentsService.getSelfPaymentsFromChannel(id1, DEFAULT_MAX_AGE)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsFromChannel(id2, DEFAULT_MAX_AGE)).thenReturn(List.of(selfPayment2));
    }

    private void mockSupportAsSourceForPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL));
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("from " + CHANNEL_ID, 0),
                getSelfPayment("from: " + CHANNEL_ID, 1),
                getSelfPayment("to: " + CHANNEL_ID_2, 2)
        ));
    }

    private void mockSupportAsTargetForPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2));
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE)).thenReturn(List.of(
                getSelfPayment("from " + CHANNEL_ID, 0),
                getSelfPayment("to " + CHANNEL_ID_2, 1),
                getSelfPayment("to " + CHANNEL_ID_2, 2)
        ));
    }
}