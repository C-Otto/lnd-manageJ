package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private GrpcChannels grpcChannels;

    @Test
    void getAvailableLocalBalance() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_CHANNEL));
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(Coins.ofSatoshis(900));
    }

    @Test
    void getAvailableLocalBalance_empty() {
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getAvailableRemoteBalance() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_CHANNEL));
        assertThat(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).isEqualTo(Coins.ofSatoshis(113));
    }

    @Test
    void getAvailableRemoteBalance_empty() {
        assertThat(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }
}