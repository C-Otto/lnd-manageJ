package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_BALANCE;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.RESERVE_LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private GrpcChannels grpcChannels;

    @Test
    void getLocalBalance() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_CHANNEL));
        assertThat(balanceService.getLocalBalance(CHANNEL_ID)).isEqualTo(LOCAL_BALANCE);
    }

    @Test
    void getLocalBalance_no_channel() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(balanceService.getLocalBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getLocalReserve() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_CHANNEL));
        assertThat(balanceService.getLocalReserve(CHANNEL_ID)).isEqualTo(RESERVE_LOCAL);
    }

    @Test
    void getAvailableLocalBalance() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_CHANNEL));
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID))
                .isEqualTo(LOCAL_BALANCE.subtract(RESERVE_LOCAL));
    }

    @Test
    void getAvailableLocalBalance_negative() {
        LocalChannel localChannel = new LocalChannel(CHANNEL, PUBKEY, Coins.ofSatoshis(99), Coins.ofSatoshis(100));
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(localChannel));
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }
}