package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcClosedChannels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_TO_NODE_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {
    @InjectMocks
    private ChannelService channelService;

    @Mock
    private GrpcChannels grpcChannels;

    @Mock
    private GrpcClosedChannels grpcClosedChannels;

    @Test
    void isClosed_false() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2));
        assertThat(channelService.isClosed(CHANNEL_ID)).isFalse();
    }

    @Test
    void isClosed() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2,
                CLOSED_CHANNEL.getId(), CLOSED_CHANNEL
        ));
        assertThat(channelService.isClosed(CHANNEL_ID)).isTrue();
    }

    @Test
    void isClosed_force_closed() {
        when(grpcClosedChannels.getClosedChannels())
                .thenReturn(Map.of(FORCE_CLOSED_CHANNEL.getId(), FORCE_CLOSED_CHANNEL));
        assertThat(channelService.isClosed(CHANNEL_ID)).isTrue();
    }

    @Test
    void getLocalChannel_unknown() {
        assertThat(channelService.getLocalChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getLocalChannel_open() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_2, LOCAL_OPEN_CHANNEL));
        assertThat(channelService.getLocalChannel(CHANNEL_ID)).contains(LOCAL_OPEN_CHANNEL);
    }

    @Test
    void getLocalChannel_waiting_close_channel() {
        when(grpcChannels.getWaitingCloseChannels()).thenReturn(Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2));
        assertThat(channelService.getLocalChannel(CHANNEL_ID)).contains(WAITING_CLOSE_CHANNEL);
    }

    @Test
    void getLocalChannel_force_closing_channel() {
        when(grpcChannels.getForceClosingChannels()).thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2));
        assertThat(channelService.getLocalChannel(CHANNEL_ID)).contains(FORCE_CLOSING_CHANNEL);
    }

    @Test
    void getLocalChannel_closed() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(CLOSED_CHANNEL.getId(), CLOSED_CHANNEL));
        assertThat(channelService.getLocalChannel(CHANNEL_ID)).contains(CLOSED_CHANNEL);
    }

    @Test
    void getOpenChannel() {
        when(grpcChannels.getChannel(CHANNEL_ID_2)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(channelService.getOpenChannel(CHANNEL_ID_2)).contains(LOCAL_OPEN_CHANNEL);
    }

    @Test
    void getOpenChannel_not_open() {
        assertThat(channelService.getOpenChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getOpenChannelsWith_by_pubkey() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3);
    }

    @Test
    void getOpenChannelsWith_ignores_channel_to_other_node() {
        when(grpcChannels.getChannels()).thenReturn(
                Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL_3)
        );
        assertThat(channelService.getOpenChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3);
    }

    @Test
    void getClosedChannelsWith_by_pubkey() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                CLOSED_CHANNEL.getId(), CLOSED_CHANNEL,
                CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2
        ));
        assertThat(channelService.getClosedChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannelsWith_ignores_channel_to_other_node() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(
                Map.of(
                        CLOSED_CHANNEL.getId(), CLOSED_CHANNEL,
                        CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2,
                        CLOSED_CHANNEL_TO_NODE_3.getId(), CLOSED_CHANNEL_TO_NODE_3
                )
        );
        assertThat(channelService.getClosedChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
    }

    @Test
    void getWaitingCloseChannelsWith_by_pubkey() {
        when(grpcChannels.getWaitingCloseChannels()).thenReturn(Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2));
        assertThat(channelService.getWaitingCloseChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2);
    }

    @Test
    void getWaitingCloseChannelsWith_ignores_channel_to_other_node() {
        when(grpcChannels.getWaitingCloseChannels()).thenReturn(
                Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2, WAITING_CLOSE_CHANNEL_TO_NODE_3)
        );
        assertThat(channelService.getWaitingCloseChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2);
    }

    @Test
    void getForceClosingChannelsWith_by_pubkey() {
        when(grpcChannels.getForceClosingChannels()).thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2));
        assertThat(channelService.getForceClosingChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2);
    }

    @Test
    void getForceClosingChannelsWith_ignores_channel_to_other_node() {
        when(grpcChannels.getForceClosingChannels()).thenReturn(
                Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2, FORCE_CLOSING_CHANNEL_TO_NODE_3)
        );
        assertThat(channelService.getForceClosingChannelsWith(PUBKEY_2))
                .containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2);
    }

    @Test
    void getOpenChannels() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(channelService.getOpenChannels())
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
    }

    @Test
    void getClosedChannels() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                CLOSED_CHANNEL.getId(), CLOSED_CHANNEL,
                CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2
        ));
        assertThat(channelService.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannel() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                CLOSED_CHANNEL.getId(), CLOSED_CHANNEL,
                CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2
        ));
        assertThat(channelService.getClosedChannel(CHANNEL_ID_2)).contains(CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannel_not_closed() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(CLOSED_CHANNEL.getId(), CLOSED_CHANNEL));
        assertThat(channelService.getClosedChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getForceClosedChannels() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                FORCE_CLOSED_CHANNEL.getId(), FORCE_CLOSED_CHANNEL,
                CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2
        ));
        assertThat(channelService.getForceClosedChannels()).containsExactly(FORCE_CLOSED_CHANNEL);
    }

    @Test
    void getForceClosedChannel() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(
                FORCE_CLOSED_CHANNEL_2.getId(), FORCE_CLOSED_CHANNEL_2,
                CLOSED_CHANNEL.getId(), CLOSED_CHANNEL
        ));
        assertThat(channelService.getForceClosedChannel(CHANNEL_ID_2)).contains(FORCE_CLOSED_CHANNEL_2);
    }

    @Test
    void getForceClosedChannel_not_force_closed() {
        when(grpcClosedChannels.getClosedChannels()).thenReturn(Map.of(CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2));
        assertThat(channelService.getForceClosedChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getForceClosingChannels() {
        when(grpcChannels.getForceClosingChannels())
                .thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2));
        assertThat(channelService.getForceClosingChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2);
    }

    @Test
    void getWaitingCloseChannels() {
        when(grpcChannels.getWaitingCloseChannels())
                .thenReturn(Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2));
        assertThat(channelService.getWaitingCloseChannels())
                .containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2);
    }

    @Test
    void getAllChannels_by_pubkey() {
        when(grpcChannels.getWaitingCloseChannels())
                .thenReturn(Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_TO_NODE_3));
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        when(grpcChannels.getForceClosingChannels())
                .thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_TO_NODE_3));
        when(grpcClosedChannels.getClosedChannels())
                .thenReturn(Map.of(
                        CLOSED_CHANNEL_2.getId(), CLOSED_CHANNEL_2,
                        CLOSED_CHANNEL_TO_NODE_3.getId(), CLOSED_CHANNEL_TO_NODE_3
                ));

        assertThat(channelService.getAllChannelsWith(PUBKEY_2)).containsExactlyInAnyOrder(
                LOCAL_OPEN_CHANNEL,
                CLOSED_CHANNEL_2,
                FORCE_CLOSING_CHANNEL,
                WAITING_CLOSE_CHANNEL
        );
    }
}