package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnChainCostsControllerTest {
    @InjectMocks
    private OnChainCostsController onChainCostsController;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private Metrics metrics;

    @Test
    void getCostsForPeer() throws CostException {
        Coins openCosts = Coins.ofSatoshis(123);
        Coins closeCosts = Coins.ofSatoshis(456);
        when(onChainCostService.getOpenCostsWith(PUBKEY)).thenReturn(openCosts);
        when(onChainCostService.getCloseCostsWith(PUBKEY)).thenReturn(closeCosts);
        OnChainCostsDto expected = new OnChainCostsDto(openCosts, closeCosts);
        assertThat(onChainCostsController.getCostsForPeer(PUBKEY)).isEqualTo(expected);
        verify(metrics).mark(argThat(name -> name.endsWith(".getCostsForPeer")));
    }

    @Test
    void getOpenCostsForChannel() throws CostException {
        Coins coins = Coins.ofSatoshis(123);
        when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(coins));
        assertThat(onChainCostsController.getOpenCostsForChannel(CHANNEL_ID)).isEqualTo(coins.satoshis());
        verify(metrics).mark(argThat(name -> name.endsWith(".getOpenCostsForChannel")));
    }

    @Test
    void getOpenCostsForChannel_unknown() {
        assertThatExceptionOfType(CostException.class).isThrownBy(
                () -> onChainCostsController.getOpenCostsForChannel(CHANNEL_ID)
        );
    }

    @Test
    void getCloseCostsForChannel() throws CostException {
        Coins coins = Coins.ofSatoshis(123);
        when(onChainCostService.getCloseCosts(CHANNEL_ID)).thenReturn(Optional.of(coins));
        assertThat(onChainCostsController.getCloseCostsForChannel(CHANNEL_ID)).isEqualTo(coins.satoshis());
        verify(metrics).mark(argThat(name -> name.endsWith(".getCloseCostsForChannel")));
    }

    @Test
    void getCloseCostsForChannel_unknown() {
        assertThatExceptionOfType(CostException.class).isThrownBy(
                () -> onChainCostsController.getCloseCostsForChannel(CHANNEL_ID)
        );
    }
}