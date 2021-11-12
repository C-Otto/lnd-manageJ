package de.cotto.lndmanagej.grpc;

import lnrpc.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelsTest {
    @InjectMocks
    private GrpcChannels grpcChannels;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        when(grpcGetInfo.getPubkey()).thenReturn(Optional.of(PUBKEY));
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
                .setRemotePubkey(NODE_2.pubkey().toString())
                .build();
    }
}