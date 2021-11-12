package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import lnrpc.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
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
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
    }

    @Test
    void getChannels_no_channels() {
        assertThat(grpcChannels.getChannels()).isEmpty();
    }

    @Test
    void getChannels_one_channel() {
        when(grpcService.getChannels()).thenReturn(List.of(channel(CHANNEL_ID)));
        assertThat(grpcChannels.getChannels()).containsExactly(LOCAL_CHANNEL);
    }

    @Test
    void getChannel() {
        when(grpcService.getChannels()).thenReturn(List.of(channel(CHANNEL_ID_2), channel(CHANNEL_ID)));
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).contains(LOCAL_CHANNEL);
    }

    @Test
    void getChannel_empty() {
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).isEmpty();
    }

    private Channel channel(ChannelId channelId) {
        return Channel.newBuilder()
                .setChanId(channelId.shortChannelId())
                .setCapacity(CAPACITY.satoshis())
                .setRemotePubkey(NODE_2.pubkey().toString())
                .build();
    }
}