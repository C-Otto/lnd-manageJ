package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OffChainCostService;
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
class OffChainCostsControllerTest {
    @InjectMocks
    private OffChainCostsController offChainCostsController;

    @Mock
    private OffChainCostService offChainCostService;

    @Test
    void getRebalanceSourceCostsForChannel() {
        when(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(123));
        assertThat(offChainCostsController.getRebalanceSourceCostsForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceSourceCostsForPeer() {
        when(offChainCostService.getRebalanceSourceCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(123));
        assertThat(offChainCostsController.getRebalanceSourceCostsForPeer(PUBKEY)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetCostsForChannel() {
        when(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(123));
        assertThat(offChainCostsController.getRebalanceTargetCostsForChannel(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getRebalanceTargetCostsForPeer() {
        when(offChainCostService.getRebalanceTargetCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(123));
        assertThat(offChainCostsController.getRebalanceTargetCostsForPeer(PUBKEY)).isEqualTo(123);
    }
}