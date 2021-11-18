package de.cotto.lndmanagej.controller;

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
}