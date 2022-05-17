package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.controller.dto.PubkeysDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.GraphService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {
    @InjectMocks
    private StatusController statusController;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private GraphService graphService;

    @Test
    void isSyncedToChain() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);

        assertThat(statusController.isSyncedToChain()).isTrue();
    }

    @Test
    void isSyncedToChain_false() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        assertThat(statusController.isSyncedToChain()).isFalse();
    }

    @Test
    void getOpenChannels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<ChannelId> expectedChannelIds =
                List.of(LOCAL_OPEN_CHANNEL.getId(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getId());
        assertThat(statusController.getOpenChannels()).isEqualTo(new ChannelsDto(expectedChannelIds));
    }

    @Test
    void getPubkeysForOpenChannels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<Pubkey> expectedPubkeys =
                List.of(LOCAL_OPEN_CHANNEL.getRemotePubkey(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey());
        assertThat(statusController.getPubkeysForOpenChannels()).isEqualTo(new PubkeysDto(expectedPubkeys));
    }

    @Test
    void getPubkeysForOpenChannels_without_no_duplicates() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(statusController.getPubkeysForOpenChannels().pubkeys()).containsExactly(PUBKEY_2.toString());
    }

    @Test
    void getAllChannels() {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<ChannelId> expectedChannelIds =
                List.of(CLOSED_CHANNEL.getId(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getId());
        assertThat(statusController.getAllChannels()).isEqualTo(new ChannelsDto(expectedChannelIds));
    }

    @Test
    void getPubkeysForAllChannels() {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<Pubkey> expectedPubkeys =
                List.of(CLOSED_CHANNEL.getRemotePubkey(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey());
        assertThat(statusController.getPubkeysForAllChannels()).isEqualTo(new PubkeysDto(expectedPubkeys));
    }

    @Test
    void getPubkeysForAllChannels_without_no_duplicates() {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(CLOSED_CHANNEL_2, CLOSED_CHANNEL));
        assertThat(statusController.getPubkeysForAllChannels().pubkeys()).containsExactly(PUBKEY_2.toString());
    }

    @Test
    void getBlockHeight() {
        when(ownNodeService.getBlockHeight()).thenReturn(123_456);
        assertThat(statusController.getBlockHeight()).isEqualTo(123_456);
    }

    @Test
    void getKnownChannels() {
        when(graphService.getNumberOfChannels()).thenReturn(123);
        assertThat(statusController.getKnownChannels()).isEqualTo(123);
    }
}
