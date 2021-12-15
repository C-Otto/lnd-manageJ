package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
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
    @InjectMocks
    private RebalanceService rebalanceService;

    @Mock
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private ChannelService channelService;

    @Test
    void getRebalancesFromPeer() {
        Set<SelfPayment> selfPayments = mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getRebalancesFromPeer(PUBKEY)).isEqualTo(selfPayments);
    }

    @Test
    void getRebalancesToPeer() {
        Set<SelfPayment> selfPayments = mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getRebalancesToPeer(PUBKEY)).isEqualTo(selfPayments);
    }

    @Test
    void getRebalanceAmountFromPeer() {
        mockTwoChannelsAndPaymentsFromPeer();
        assertThat(rebalanceService.getRebalanceAmountFromPeer(PUBKEY)).isEqualTo(AMOUNT_PAID.add(AMOUNT_PAID));
    }

    @Test
    void getRebalanceAmountToPeer() {
        mockTwoChannelsAndPaymentsToPeer();
        assertThat(rebalanceService.getRebalanceAmountToPeer(PUBKEY)).isEqualTo(AMOUNT_PAID.add(AMOUNT_PAID));
    }

    @Test
    void getRebalanceAmountFromChannel() {
        mockSelfPaymentsFromChannel("from " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getRebalanceAmountFromChannel(CHANNEL_ID)).isEqualTo(AMOUNT_PAID.add(AMOUNT_PAID));
    }

    @Test
    void getRebalanceAmountToChannel() {
        mockSelfPaymentsToChannel("to " + CHANNEL_ID_2.getShortChannelId());
        assertThat(rebalanceService.getRebalanceAmountToChannel(CHANNEL_ID_2)).isEqualTo(AMOUNT_PAID.add(AMOUNT_PAID));
    }

    @Test
    void getRebalancesFromChannel_short_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getShortChannelId());
        assertThat(rebalanceService.getRebalancesFromChannel(CHANNEL_ID)).hasSize(2);
    }

    @Test
    void getRebalanceSourceCostsForChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactForm());
        assertThat(rebalanceService.getRebalancesFromChannel(CHANNEL_ID)).hasSize(2);
    }

    @Test
    void getRebalanceSourceCostsForChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactFormLnd());
        assertThat(rebalanceService.getRebalancesFromChannel(CHANNEL_ID)).hasSize(2);
    }

    @Test
    void getRebalanceSourceCostsForChannel_id_not_in_memo() {
        mockSelfPaymentsFromChannel("something");
        assertThat(rebalanceService.getRebalancesFromChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRebalancesToChannel_no_self_payments() {
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getRebalancesToChannel_short_channel_id_in_memo() {
        mockSelfPaymentsToChannel("foo bar " + CHANNEL_ID_2.getShortChannelId() + "!");
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).hasSize(2);
    }

    @Test
    void getRebalancesToChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsToChannel("something something " + CHANNEL_ID_2.getCompactForm());
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).hasSize(2);
    }

    @Test
    void getRebalancesToChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsToChannel(CHANNEL_ID_2.getCompactFormLnd());
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).hasSize(2);
    }

    @Test
    void getRebalancesToChannel_id_not_in_memo() {
        mockSelfPaymentsToChannel("something");
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).hasSize(2);
    }

    @Test
    void getRebalancesToChannel_other_channel_id_in_memo() {
        mockSelfPaymentsToChannel("rebalance to " + CHANNEL_ID_3);
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).hasSize(2);
    }

    @Test
    void getRebalancesToChannel_source_channel_id_in_memo() {
        mockSelfPaymentsToChannel("something: " + CHANNEL_ID);
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getRebalancesToChannel_without_first_channel_information() {
        Payment payment = new Payment(
                PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, List.of()
        );
        SettledInvoice settledInvoice = new SettledInvoice(
                ADD_INDEX,
                SETTLE_INDEX,
                SETTLE_DATE,
                HASH,
                AMOUNT_PAID,
                "something: " + CHANNEL_ID,
                Optional.empty(),
                Optional.of(CHANNEL_ID_2)
        );
        SelfPayment selfPayment = new SelfPayment(payment, settledInvoice);
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2)).thenReturn(List.of(selfPayment));
        assertThat(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).containsExactly(selfPayment);
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
        SettledInvoice settledInvoice = new SettledInvoice(
                ADD_INDEX,
                SETTLE_INDEX,
                SETTLE_DATE.plusSeconds(offset),
                HASH,
                AMOUNT_PAID,
                memo,
                Optional.empty(),
                Optional.of(CHANNEL_ID_2)
        );
        return new SelfPayment(payment, settledInvoice);
    }

    private List<PaymentRoute> getSingleRoute() {
        PaymentHop firstHop = new PaymentHop(CHANNEL_ID, Coins.NONE);
        PaymentHop lastHop = new PaymentHop(CHANNEL_ID_2, Coins.NONE);
        PaymentRoute route = new PaymentRoute(List.of(firstHop, lastHop));
        return List.of(route);
    }

    private Set<SelfPayment> mockTwoChannelsAndPaymentsToPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL_3.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_3, CLOSED_CHANNEL_2));
        SelfPayment selfPayment1 = getSelfPayment(id1.toString(), 0);
        SelfPayment selfPayment2 = getSelfPayment(id2.toString(), 1);
        Set<SelfPayment> selfPayments = Set.of(selfPayment1, selfPayment2);
        when(selfPaymentsService.getSelfPaymentsToChannel(id1)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsToChannel(id2)).thenReturn(List.of(selfPayment2));
        return selfPayments;
    }

    private Set<SelfPayment> mockTwoChannelsAndPaymentsFromPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        SelfPayment selfPayment1 = getSelfPayment(id1.toString(), 0);
        SelfPayment selfPayment2 = getSelfPayment(id2.toString(), 1);
        Set<SelfPayment> selfPayments = Set.of(selfPayment1, selfPayment2);
        when(selfPaymentsService.getSelfPaymentsFromChannel(id1)).thenReturn(List.of(selfPayment1));
        when(selfPaymentsService.getSelfPaymentsFromChannel(id2)).thenReturn(List.of(selfPayment2));
        return selfPayments;
    }
}