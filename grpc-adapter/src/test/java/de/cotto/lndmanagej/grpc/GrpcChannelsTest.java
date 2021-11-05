package de.cotto.lndmanagej.grpc;

import lnrpc.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelsTest {
    private GrpcChannels grpcChannels;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        when(grpcGetInfo.getNode()).thenReturn(NODE);
        grpcChannels = new GrpcChannels(grpcService, grpcGetInfo);
    }

    @Test
    void no_channels() {
        assertThat(grpcChannels.getChannels()).isEmpty();
    }

    @Test
    void one_channel() {
        when(grpcService.getChannels()).thenReturn(List.of(channel()));
        assertThat(grpcChannels.getChannels()).containsExactly(CHANNEL);
    }

    private Channel channel() {
        return Channel.newBuilder()
                .setChanId(CHANNEL_ID.shortChannelId())
                .setCapacity(CAPACITY.satoshis())
                .setRemotePubkey(NODE_2.pubkey())
                .build();
    }
}