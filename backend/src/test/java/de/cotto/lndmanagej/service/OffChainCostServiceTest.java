package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OffChainCosts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffChainCostServiceTest {
    private static final Coins COST_FOR_TWO_SELF_PAYMENTS = SELF_PAYMENT.fees().add(SELF_PAYMENT_2.fees());

    @InjectMocks
    private OffChainCostService offChainCostService;

    @Mock
    private RebalanceService rebalanceService;

    @Test
    void getOffChainCostsForPeer_source() {
        when(rebalanceService.getRebalancesFromPeer(PUBKEY)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        OffChainCosts expected = new OffChainCosts(
                COST_FOR_TWO_SELF_PAYMENTS,
                Coins.NONE
        );
        assertThat(offChainCostService.getOffChainCostsForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getOffChainCostsForPeer_target() {
        when(rebalanceService.getRebalancesToPeer(PUBKEY)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        OffChainCosts expected = new OffChainCosts(
                Coins.NONE,
                COST_FOR_TWO_SELF_PAYMENTS
        );
        assertThat(offChainCostService.getOffChainCostsForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getOffChainCostsForChannel_source() {
        when(rebalanceService.getRebalancesFromChannel(CHANNEL_ID_4)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        OffChainCosts expected = new OffChainCosts(
                COST_FOR_TWO_SELF_PAYMENTS,
                Coins.NONE
        );
        assertThat(offChainCostService.getOffChainCostsForChannel(CHANNEL_ID_4)).isEqualTo(expected);
    }

    @Test
    void getOffChainCostsForChannel_target() {
        when(rebalanceService.getRebalancesToChannel(CHANNEL_ID_4)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        OffChainCosts expected = new OffChainCosts(
                Coins.NONE,
                COST_FOR_TWO_SELF_PAYMENTS
        );
        assertThat(offChainCostService.getOffChainCostsForChannel(CHANNEL_ID_4)).isEqualTo(expected);
    }

    @Test
    void getRebalanceSourceCostsForChannel_no_self_payments() {
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceSourceCostsForChannel() {
        when(rebalanceService.getRebalancesFromChannel(CHANNEL_ID)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceSourceCostsForPeer() {
        when(rebalanceService.getRebalancesFromPeer(PUBKEY)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(offChainCostService.getRebalanceSourceCostsForPeer(PUBKEY))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForChannel_no_self_payments() {
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2)).isEqualTo(Coins.NONE);
    }

    @Test
    void getRebalanceTargetCostsForChannel() {
        when(rebalanceService.getRebalancesToChannel(CHANNEL_ID_2)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID_2))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

    @Test
    void getRebalanceTargetCostsForPeer() {
        when(rebalanceService.getRebalancesToPeer(PUBKEY)).thenReturn(Set.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(offChainCostService.getRebalanceTargetCostsForPeer(PUBKEY))
                .isEqualTo(COST_FOR_TWO_SELF_PAYMENTS);
    }

}