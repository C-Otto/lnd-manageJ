package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeControllerTest {
    @InjectMocks
    private NodeController nodeController;

    @Mock
    private NodeService nodeService;

    @Mock
    private Metrics metrics;

    @Mock
    private ChannelService channelService;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);

        assertThat(nodeController.getAlias(PUBKEY_2)).isEqualTo(ALIAS_2);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAlias")));
    }

    @Test
    void getNodeDetails_no_channels() {
        NodeDetailsDto expectedDetails = new NodeDetailsDto(
                PUBKEY_2,
                ALIAS_2,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                true
        );
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, true));

        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(expectedDetails);
        verify(metrics).mark(argThat(name -> name.endsWith(".getDetails")));
    }

    @Test
    void getNodeDetails_with_channels() {
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, false));
        when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        when(channelService.getClosedChannelsWith(PUBKEY_2)).thenReturn(Set.of(CLOSED_CHANNEL_2, CLOSED_CHANNEL_3));
        when(channelService.getWaitingCloseChannelsFor(PUBKEY_2)).thenReturn(
                Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2)
        );
        when(channelService.getForceClosingChannelsFor(PUBKEY_2)).thenReturn(
                Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2, FORCE_CLOSING_CHANNEL_3)
        );
        NodeDetailsDto expectedDetails = new NodeDetailsDto(
                PUBKEY_2,
                ALIAS_2,
                List.of(CHANNEL_ID, CHANNEL_ID_3),
                List.of(CHANNEL_ID_2, CHANNEL_ID_3),
                List.of(CHANNEL_ID, CHANNEL_ID_2),
                List.of(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3),
                false
        );

        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(expectedDetails);
    }

    @Test
    void getOpenChannelIds_for_peer() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(nodeController.getOpenChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_3)));
        verify(metrics).mark(argThat(name -> name.endsWith(".getOpenChannelIdsForPubkey")));
    }

    @Test
    void getOpenChannelIds_for_peer_ordered() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_2, LOCAL_OPEN_CHANNEL));
        assertThat(nodeController.getOpenChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_2)));
    }
}