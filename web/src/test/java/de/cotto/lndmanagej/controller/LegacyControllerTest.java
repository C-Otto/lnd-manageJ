package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeConfigurationFixtures;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_4;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyControllerTest {
    @InjectMocks
    private LegacyController legacyController;

    @Mock
    private NodeService nodeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private FeeService feeService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private Metrics metrics;

    @Test
    void getAllChannelIds_for_peer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        assertThat(legacyController.getAllChannelIdsForPubkey(PUBKEY)).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
        verify(metrics).mark(argThat(name -> name.endsWith(".getAllChannelIdsForPubkey")));
    }

    @Test
    void getAllChannelIds_for_peer_ordered() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getAllChannelIdsForPubkey(PUBKEY)).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getOpenChannelIds() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(legacyController.getOpenChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
        verify(metrics).mark(argThat(name -> name.endsWith(".getOpenChannelIds")));
    }

    @Test
    void getOpenChannelIds_ordered() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getOpenChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getOpenChannelIdsPretty() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(nodeService.getAlias(PUBKEY_3)).thenReturn(ALIAS_3);
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        assertThat(legacyController.getOpenChannelIdsPretty()).isEqualTo(
                CHANNEL_ID_COMPACT + "\t" + PUBKEY_2 + "\t" + CAPACITY + "\t" + ALIAS_2 + "\n" +
                        CHANNEL_ID_COMPACT_4 + "\t" + PUBKEY_3 + "\t" + CAPACITY_2 + "\t" + ALIAS_3
        );
        verify(metrics).mark(argThat(name -> name.endsWith(".getOpenChannelIdsPretty")));
    }

    @Test
    void getOpenChannelIdsPretty_ordered() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getOpenChannelIdsPretty())
                .matches(CHANNEL_ID_COMPACT + ".*\n" + CHANNEL_ID_COMPACT_4 + ".*");
    }

    @Test
    void getClosedChannelIds() {
        when(channelService.getClosedChannels()).thenReturn(Set.of(CLOSED_CHANNEL, CLOSED_CHANNEL_3));
        assertThat(legacyController.getClosedChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
        verify(metrics).mark(argThat(name -> name.endsWith(".getClosedChannelIds")));
    }

    @Test
    void getClosedChannelIds_ordered() {
        when(channelService.getClosedChannels()).thenReturn(Set.of(CLOSED_CHANNEL_3, CLOSED_CHANNEL));
        assertThat(legacyController.getClosedChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getForceClosingChannelIds() {
        when(channelService.getForceClosingChannels())
                .thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_3));
        assertThat(legacyController.getForceClosingChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
        verify(metrics).mark(argThat(name -> name.endsWith(".getForceClosingChannelIds")));
    }

    @Test
    void getForceClosingChannelIds_ordered() {
        when(channelService.getForceClosingChannels())
                .thenReturn(Set.of(FORCE_CLOSING_CHANNEL_3, FORCE_CLOSING_CHANNEL));
        assertThat(legacyController.getForceClosingChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getPeerPubkeys() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2 + "\n" + PUBKEY_3);
        verify(metrics).mark(argThat(name -> name.endsWith(".getPeerPubkeys")));
    }

    @Test
    void getPeerPubkeys_sorted() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2 + "\n" + PUBKEY_3);
    }

    @Test
    void getPeerPubkeys_without_duplicates() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2.toString());
    }

    @Test
    void getOutgoingFeeRate() {
        when(feeService.getFeeConfiguration(CHANNEL_ID)).thenReturn(FeeConfigurationFixtures.FEE_CONFIGURATION);
        assertThat(legacyController.getOutgoingFeeRate(CHANNEL_ID)).isEqualTo(1);
        verify(metrics).mark(argThat(name -> name.endsWith(".getOutgoingFeeRate")));
    }

    @Test
    void getOutgoingBaseFee() {
        when(feeService.getFeeConfiguration(CHANNEL_ID)).thenReturn(FeeConfigurationFixtures.FEE_CONFIGURATION);
        assertThat(legacyController.getOutgoingBaseFee(CHANNEL_ID)).isEqualTo(2);
        verify(metrics).mark(argThat(name -> name.endsWith(".getOutgoingBaseFee")));
    }

    @Test
    void getIncomingFeeRate() {
        when(feeService.getFeeConfiguration(CHANNEL_ID)).thenReturn(FeeConfigurationFixtures.FEE_CONFIGURATION);
        assertThat(legacyController.getIncomingFeeRate(CHANNEL_ID)).isEqualTo(3);
        verify(metrics).mark(argThat(name -> name.endsWith(".getIncomingFeeRate")));
    }

    @Test
    void getIncomingBaseFee() {
        when(feeService.getFeeConfiguration(CHANNEL_ID)).thenReturn(FeeConfigurationFixtures.FEE_CONFIGURATION);
        assertThat(legacyController.getIncomingBaseFee(CHANNEL_ID)).isEqualTo(4);
        verify(metrics).mark(argThat(name -> name.endsWith(".getIncomingBaseFee")));
    }

    @Test
    void getAvailableLocalBalance_channel() {
        when(balanceService.getAvailableLocalBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(123L));
        assertThat(legacyController.getAvailableLocalBalanceForChannel(CHANNEL_ID)).isEqualTo(123);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAvailableLocalBalanceForChannel")));
    }

    @Test
    void getAvailableRemoteBalance_channel() {
        when(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(123L));
        assertThat(legacyController.getAvailableRemoteBalanceForChannel(CHANNEL_ID)).isEqualTo(123);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAvailableRemoteBalanceForChannel")));
    }

    @Test
    void getAvailableLocalBalance_peer() {
        when(balanceService.getAvailableLocalBalance(PUBKEY)).thenReturn(Coins.ofSatoshis(246L));
        assertThat(legacyController.getAvailableLocalBalanceForPeer(PUBKEY)).isEqualTo(246);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAvailableLocalBalanceForPeer")));
    }

    @Test
    void getAvailableRemoteBalance_peer() {
        when(balanceService.getAvailableRemoteBalance(PUBKEY)).thenReturn(Coins.ofSatoshis(246L));
        assertThat(legacyController.getAvailableRemoteBalanceForPeer(PUBKEY)).isEqualTo(246);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAvailableRemoteBalanceForPeer")));
    }
}