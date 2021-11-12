package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelFixtures;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_4;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
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
    private OwnNodeService ownNodeService;

    @Mock
    private FeeService feeService;

    @Mock
    private BalanceService balanceService;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS);
        assertThat(legacyController.getAlias(PUBKEY)).isEqualTo(ALIAS);
    }

    @Test
    void getOpenChannelIds_for_peer() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_3));
        assertThat(legacyController.getOpenChannelIds(PUBKEY)).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getOpenChannelIds_for_peer_ordered() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_CHANNEL_3, LOCAL_CHANNEL));
        assertThat(legacyController.getOpenChannelIds(PUBKEY)).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getOpenChannelIds() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_3));
        assertThat(legacyController.getOpenChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void getOpenChannelIdsCompact() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_3));
        assertThat(legacyController.getOpenChannelIdsCompact()).isEqualTo(
                CHANNEL_ID_COMPACT + "\n" + CHANNEL_ID_COMPACT_3
        );
    }

    @Test
    void getOpenChannelIdsPretty() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(nodeService.getAlias(PUBKEY_3)).thenReturn(ALIAS_3);
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_TO_NODE_3));
        assertThat(legacyController.getOpenChannelIdsPretty()).isEqualTo(
                CHANNEL_ID_COMPACT + "\t" + PUBKEY_2 + "\t" + CAPACITY + "\t" + ALIAS_2 + "\n" +
                        CHANNEL_ID_COMPACT_4 + "\t" + PUBKEY_3 + "\t" + CAPACITY_2 + "\t" + ALIAS_3
        );
    }

    @Test
    void getOpenChannelIdsPretty_sorted() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL_TO_NODE_3, LOCAL_CHANNEL));
        assertThat(legacyController.getOpenChannelIdsPretty())
                .matches(CHANNEL_ID_COMPACT + ".*\n" + CHANNEL_ID_COMPACT_4 + ".*");
    }

    @Test
    void getOpenChannelIds_ordered() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL_3, LOCAL_CHANNEL));
        assertThat(legacyController.getOpenChannelIds()).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }

    @Test
    void syncedToChain() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        assertThat(legacyController.syncedToChain()).isTrue();
    }

    @Test
    void syncedToChain_false() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        assertThat(legacyController.syncedToChain()).isFalse();
    }

    @Test
    void getPeerPubkeys() {
        Channel channel = ChannelFixtures.create(PUBKEY, PUBKEY_3, CHANNEL_ID_2);
        LocalChannel channel2 = new LocalChannel(channel, PUBKEY, BALANCE_INFORMATION);
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, channel2));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2 + "\n" + PUBKEY_3);
    }

    @Test
    void getPeerPubkeys_sorted() {
        Channel channel = ChannelFixtures.create(PUBKEY, PUBKEY_3, CHANNEL_ID_2);
        LocalChannel channel2 = new LocalChannel(channel, PUBKEY, BALANCE_INFORMATION);
        when(channelService.getOpenChannels()).thenReturn(Set.of(channel2, LOCAL_CHANNEL));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2 + "\n" + PUBKEY_3);
    }

    @Test
    void getPeerPubkeys_without_duplicates() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_2));
        assertThat(legacyController.getPeerPubkeys()).isEqualTo(PUBKEY_2.toString());
    }

    @Test
    void getIncomingFeeRate() {
        when(feeService.getIncomingFeeRate(CHANNEL_ID)).thenReturn(123L);
        assertThat(legacyController.getIncomingFeeRate(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getOutgoingFeeRate() {
        when(feeService.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(123L);
        assertThat(legacyController.getOutgoingFeeRate(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getIncomingBaseFee() {
        when(feeService.getIncomingBaseFee(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(10L));
        assertThat(legacyController.getIncomingBaseFee(CHANNEL_ID)).isEqualTo(10);
    }

    @Test
    void getOutgoingBaseFee() {
        when(feeService.getOutgoingBaseFee(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(10L));
        assertThat(legacyController.getOutgoingBaseFee(CHANNEL_ID)).isEqualTo(10);
    }

    @Test
    void getAvailableLocalBalance() {
        when(balanceService.getAvailableLocalBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(123L));
        assertThat(legacyController.getAvailableLocalBalance(CHANNEL_ID)).isEqualTo(123);
    }

    @Test
    void getAvailableRemoteBalance() {
        when(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(123L));
        assertThat(legacyController.getAvailableRemoteBalance(CHANNEL_ID)).isEqualTo(123);
    }
}