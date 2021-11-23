package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_MORE_BALANCE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_MORE_BALANCE_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private GrpcChannels grpcChannels;

    @Mock
    private ChannelService channelService;

    @Test
    void getBalanceInformation_for_pubkey() {
        BalanceInformation expected = new BalanceInformation(
                Coins.ofSatoshis(4_000),
                Coins.ofSatoshis(400),
                Coins.ofSatoshis(446),
                Coins.ofSatoshis(40)
        );
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_MORE_BALANCE));
        when(grpcChannels.getChannel(CHANNEL_ID_2)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_MORE_BALANCE_2));
        assertThat(balanceService.getBalanceInformation(PUBKEY)).isEqualTo(expected);
    }

    @Test
    void getBalanceInformation_for_channel() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_MORE_BALANCE));
        assertThat(balanceService.getBalanceInformation(CHANNEL_ID))
                .contains(LOCAL_OPEN_CHANNEL_MORE_BALANCE.getBalanceInformation());
    }

    @Test
    void getBalanceInformation_for_channel_empty() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(balanceService.getBalanceInformation(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getAvailableLocalBalance_channel() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(Coins.ofSatoshis(900));
    }

    @Test
    void getAvailableLocalBalance_channel_empty() {
        assertThat(balanceService.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getAvailableRemoteBalance_channel() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).isEqualTo(Coins.ofSatoshis(113));
    }

    @Test
    void getAvailableRemoteBalance_channel_empty() {
        assertThat(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getAvailableLocalBalance_peer() {
        mockChannels();
        assertThat(balanceService.getAvailableLocalBalance(PUBKEY)).isEqualTo(Coins.ofSatoshis(1_800));
    }

    @Test
    void getAvailableLocalBalance_peer_empty() {
        assertThat(balanceService.getAvailableLocalBalance(PUBKEY)).isEqualTo(Coins.NONE);
    }

    @Test
    void getAvailableRemoteBalance_peer() {
        mockChannels();
        assertThat(balanceService.getAvailableRemoteBalance(PUBKEY)).isEqualTo(Coins.ofSatoshis(226));
    }

    @Test
    void getAvailableRemoteBalance_peer_empty() {
        assertThat(balanceService.getAvailableRemoteBalance(PUBKEY)).isEqualTo(Coins.NONE);
    }

    private void mockChannels() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(grpcChannels.getChannel(CHANNEL_ID_2)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
    }
}