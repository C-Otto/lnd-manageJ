package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcClosedChannels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_TO_NODE_3;
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
    void getOpenChannels() {
        when(grpcChannels.getChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(channelService.getOpenChannels())
                .containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
    }

    @Test
    void getClosedChannels() {
        when(grpcClosedChannels.getClosedChannels())
                .thenReturn(Set.of(CLOSED_CHANNEL, CLOSED_CHANNEL_2));
        assertThat(channelService.getClosedChannels())
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
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
                .thenReturn(Set.of(CLOSED_CHANNEL_3, CLOSED_CHANNEL_TO_NODE_3));

        assertThat(channelService.getAllChannelsWith(PUBKEY_2)).containsExactlyInAnyOrder(
                LOCAL_OPEN_CHANNEL,
                CLOSED_CHANNEL_3,
                FORCE_CLOSING_CHANNEL,
                WAITING_CLOSE_CHANNEL
        );
    }
}