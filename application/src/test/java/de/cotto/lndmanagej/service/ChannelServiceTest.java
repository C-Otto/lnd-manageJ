package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
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
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    @InjectMocks
    private ChannelService channelService;

    @Mock
    private GrpcChannels grpcChannels;

    @Test
    void getOpenChannelsWith_by_pubkey() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL, CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void getOpenChannelsWith_by_node() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL, CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(NODE_2)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void getOpenChannelsWith_ignores_channel_to_other_node() {
        Channel channel2 = ChannelFixtures.create(NODE, NODE_3, CHANNEL_ID_2);
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL, channel2, CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(NODE_2)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void getOpenChannelsWith_ordered() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(CHANNEL_3, CHANNEL));
        assertThat(channelService.getOpenChannelsWith(NODE_2)).containsExactly(CHANNEL_ID, CHANNEL_ID_3);
    }
}