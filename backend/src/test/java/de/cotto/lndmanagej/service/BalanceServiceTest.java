package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.balances.BalancesDao;
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

    @Mock
    private BalancesDao balancesDao;

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
        assertThat(balanceService.getBalanceInformationForPeer(PUBKEY)).isEqualTo(expected);
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
        assertThat(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).isEqualTo(Coins.ofSatoshis(1_800));
    }

    @Test
    void getAvailableLocalBalance_peer_empty() {
        assertThat(balanceService.getAvailableLocalBalanceForPeer(PUBKEY)).isEqualTo(Coins.NONE);
    }

    @Test
    void getAvailableRemoteBalance_peer() {
        mockChannels();
        assertThat(balanceService.getAvailableRemoteBalanceForPeer(PUBKEY)).isEqualTo(Coins.ofSatoshis(226));
    }

    @Test
    void getAvailableRemoteBalance_peer_empty() {
        assertThat(balanceService.getAvailableRemoteBalanceForPeer(PUBKEY)).isEqualTo(Coins.NONE);
    }

    @Test
    void getLocalBalanceMinimum_empty() {
        assertThat(balanceService.getLocalBalanceMinimum(CHANNEL_ID, 7)).isEmpty();
    }

    @Test
    void getLocalBalanceMinimum() {
        int days = 7;
        Coins coins = Coins.ofSatoshis(123);
        when(balancesDao.getLocalBalanceMinimum(CHANNEL_ID, days)).thenReturn(Optional.of(coins));
        assertThat(balanceService.getLocalBalanceMinimum(CHANNEL_ID, days)).contains(coins);
    }

    @Test
    void getLocalBalanceMaximum_empty() {
        assertThat(balanceService.getLocalBalanceMaximum(CHANNEL_ID, 7)).isEmpty();
    }

    @Test
    void getLocalBalanceMaximum() {
        int days = 7;
        Coins coins = Coins.ofSatoshis(123);
        when(balancesDao.getLocalBalanceMaximum(CHANNEL_ID, days)).thenReturn(Optional.of(coins));
        assertThat(balanceService.getLocalBalanceMaximum(CHANNEL_ID, days)).contains(coins);
    }

    @Test
    void getLocalBalanceAverage_empty() {
        assertThat(balanceService.getLocalBalanceAverage(CHANNEL_ID, 14)).isEmpty();
    }

    @Test
    void getLocalBalanceAverage() {
        int days = 14;
        Coins coins = Coins.ofSatoshis(456);
        when(balancesDao.getLocalBalanceAverage(CHANNEL_ID, days)).thenReturn(Optional.of(coins));
        assertThat(balanceService.getLocalBalanceAverage(CHANNEL_ID, days)).contains(coins);
    }

    private void mockChannels() {
        when(grpcChannels.getChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(grpcChannels.getChannel(CHANNEL_ID_2)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
    }
}
