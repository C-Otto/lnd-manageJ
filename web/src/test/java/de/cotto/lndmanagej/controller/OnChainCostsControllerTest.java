package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnChainCostsControllerTest {
    @InjectMocks
    private OnChainCostsController onChainCostsController;

    @Mock
    private OnChainCostService onChainCostService;

    @Test
    void getCostsForPeer() {
        when(onChainCostService.getOnChainCostsForPeer(PUBKEY)).thenReturn(ON_CHAIN_COSTS);
        assertThat(onChainCostsController.getCostsForPeer(PUBKEY))
                .isEqualTo(OnChainCostsDto.createFromModel(ON_CHAIN_COSTS));
    }

    @Test
    void getOpenCostsForChannel() throws CostException {
        Coins coins = Coins.ofSatoshis(123);
        when(onChainCostService.getOpenCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(coins));
        assertThat(onChainCostsController.getOpenCostsForChannel(CHANNEL_ID)).isEqualTo(coins.satoshis());
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
        when(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(coins));
        assertThat(onChainCostsController.getCloseCostsForChannel(CHANNEL_ID)).isEqualTo(coins.satoshis());
    }

    @Test
    void getCloseCostsForChannel_unknown() {
        assertThatExceptionOfType(CostException.class).isThrownBy(
                () -> onChainCostsController.getCloseCostsForChannel(CHANNEL_ID)
        );
    }
}