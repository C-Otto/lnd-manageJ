package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class NodeServiceTest {
    @InjectMocks
    private NodeService nodeService;

    @Mock
    private GrpcNodeInfo grpcNodeInfo;

    @Mock
    private GrpcChannels grpcChannels;

    @Test
    void getAlias() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE);
        assertThat(nodeService.getAlias(PUBKEY)).isEqualTo(ALIAS);
    }

    @Test
    void getOpenChannelIds() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE_2);
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL, CHANNEL_3));
        assertThat(nodeService.getOpenChannelIds(PUBKEY)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void getOpenChannelIds_ordered() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE_2);
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL_3, CHANNEL));
        assertThat(nodeService.getOpenChannelIds(PUBKEY)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void getOpenChannelIds_ignores_channel_to_other_node() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE_2);
        Channel channel2 = ChannelFixtures.create(NODE, NODE_3, CHANNEL_ID_2);
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL, channel2, CHANNEL_3));
        assertThat(nodeService.getOpenChannelIds(PUBKEY)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }
}