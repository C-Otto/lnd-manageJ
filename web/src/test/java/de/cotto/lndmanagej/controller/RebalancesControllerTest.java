package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OffChainCostService;
import de.cotto.lndmanagej.service.RebalanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebalancesControllerTest {
    private static final Coins COINS = Coins.ofMilliSatoshis(123);

    @InjectMocks
    private RebalancesController rebalancesController;

    @Mock
    private OffChainCostService offChainCostService;

    @Mock
    private RebalanceService rebalanceService;

    @Test
    void getRebalanceSourceCostsForChannel() {
        when(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceSourceCostsForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceSourceCostsForPeer() {
        when(offChainCostService.getRebalanceSourceCostsForPeer(PUBKEY)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceSourceCostsForPeer(PUBKEY)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetCostsForChannel() {
        when(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceTargetCostsForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetCostsForPeer() {
        when(offChainCostService.getRebalanceTargetCostsForPeer(PUBKEY)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceTargetCostsForPeer(PUBKEY)).isEqualTo(123);
    }

    @Test
    void getRebalanceSourceAmountForChannel() {
        when(rebalanceService.getRebalanceAmountFromChannel(CHANNEL_ID)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceSourceAmountForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceSourceAmountForPeer() {
        when(rebalanceService.getRebalanceAmountFromPeer(PUBKEY)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceSourceAmountForPeer(PUBKEY)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetAmountForChannel() {
        when(rebalanceService.getRebalanceAmountToChannel(CHANNEL_ID)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceTargetAmountForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetAmountForPeer() {
        when(rebalanceService.getRebalanceAmountToPeer(PUBKEY)).thenReturn(COINS);
        assertThat(rebalancesController.getRebalanceTargetAmountForPeer(PUBKEY)).isEqualTo(123);
    }
}