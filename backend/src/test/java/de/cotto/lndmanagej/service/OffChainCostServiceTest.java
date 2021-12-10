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
class OffChainCostServiceTest {
    private static final Coins COST_FOR_TWO_SELF_PAYMENTS = PAYMENT_FEES.add(PAYMENT_FEES);

    @InjectMocks
    private OffChainCostService offChainCostService;

    @Mock
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private ChannelService channelService;

    @Test
    void getRebalanceSourceCostsForChannel_no_self_payments() {
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceSourceCostsForChannel_short_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getShortChannelId());
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceSourceCostsForChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactForm());
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceSourceCostsForChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsFromChannel("rebalance from " + CHANNEL_ID.getCompactFormLnd());
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceSourceCostsForChannel_id_not_in_memo() {
        mockSelfPaymentsFromChannel("something");
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceSourceCostsForPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsService.getSelfPaymentsFromChannel(id1)).thenReturn(List.of(getSelfPayment(id1.toString())));
        when(selfPaymentsService.getSelfPaymentsFromChannel(id2)).thenReturn(List.of(getSelfPayment(id2.toString())));
        assertThat(offChainCostService.getRebalanceSourceCostsForPeer(PUBKEY))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_no_self_payments() {
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceTargetCostsForChannel_short_channel_id_in_memo() {
        mockSelfPaymentsToChannel("foo bar " + CHANNEL_ID_2.getShortChannelId() + "!");
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_compact_channel_id_in_memo() {
        mockSelfPaymentsToChannel("something something " + CHANNEL_ID_2.getCompactForm());
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_compact_lnd_channel_id_in_memo() {
        mockSelfPaymentsToChannel(CHANNEL_ID_2.getCompactFormLnd());
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_id_not_in_memo() {
        mockSelfPaymentsToChannel("something");
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_other_channel_id_in_memo() {
        mockSelfPaymentsToChannel("rebalance to " + CHANNEL_ID_3);
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_source_channel_id_in_memo() {
        mockSelfPaymentsToChannel("something: " + CHANNEL_ID);
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceTargetCostsForChannel_without_first_channel_information() {
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
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(PAYMENT_FEES);
    }

    @Test
    void getRebalanceTargetCostsForPeer() {
        ChannelId id1 = LOCAL_OPEN_CHANNEL.getId();
        ChannelId id2 = CLOSED_CHANNEL_2.getId();
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsService.getSelfPaymentsToChannel(id1)).thenReturn(List.of(getSelfPayment("")));
        when(selfPaymentsService.getSelfPaymentsToChannel(id2)).thenReturn(List.of(getSelfPayment("")));
        assertThat(offChainCostService.getRebalanceTargetCostsForPeer(PUBKEY))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    private void mockSelfPaymentsFromChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(
                getSelfPayment(memo),
                getSelfPayment(memo)
        ));
    }

    private void mockSelfPaymentsToChannel(String memo) {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2)).thenReturn(List.of(
                getSelfPayment(memo),
                getSelfPayment(memo)
        ));
    }

    private SelfPayment getSelfPayment(String memo) {
        List<PaymentRoute> routes = getSingleRoute();
        Payment payment = new Payment(
                PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, routes
        );
        SettledInvoice settledInvoice = new SettledInvoice(
                ADD_INDEX,
                SETTLE_INDEX,
                SETTLE_DATE,
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
}