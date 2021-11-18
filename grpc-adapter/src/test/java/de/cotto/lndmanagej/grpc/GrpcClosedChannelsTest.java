package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.ClosedChannelFixtures;
import de.cotto.lndmanagej.model.ForceClosedChannelBuilder;
import de.cotto.lndmanagej.model.OpenInitiator;
import lnrpc.ChannelCloseSummary;
import lnrpc.Initiator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2_SHORT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_SHORT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_BREACH;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_LOCAL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static lnrpc.ChannelCloseSummary.ClosureType.ABANDONED;
import static lnrpc.ChannelCloseSummary.ClosureType.BREACH_CLOSE;
import static lnrpc.ChannelCloseSummary.ClosureType.COOPERATIVE_CLOSE;
import static lnrpc.ChannelCloseSummary.ClosureType.FUNDING_CANCELED;
import static lnrpc.ChannelCloseSummary.ClosureType.LOCAL_FORCE_CLOSE;
import static lnrpc.ChannelCloseSummary.ClosureType.REMOTE_FORCE_CLOSE;
import static lnrpc.Initiator.INITIATOR_LOCAL;
import static lnrpc.Initiator.INITIATOR_REMOTE;
import static lnrpc.Initiator.INITIATOR_UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcClosedChannelsTest {
    @InjectMocks
    private GrpcClosedChannels grpcClosedChannels;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @BeforeEach
    void setUp() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
    }

    @Test
    void getClosedChannels() {
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(
                        closedChannel(CHANNEL_ID_SHORT, COOPERATIVE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE),
                        closedChannel(CHANNEL_ID_2_SHORT, COOPERATIVE_CLOSE, INITIATOR_UNKNOWN, INITIATOR_UNKNOWN)
                )
        );
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getClosedChannels_close_initiator_unknown_but_force_close_local() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, LOCAL_FORCE_CLOSE, INITIATOR_UNKNOWN, INITIATOR_UNKNOWN)
        ));
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                        .withOpenInitiator(OpenInitiator.UNKNOWN)
                        .withCloseInitiator(CloseInitiator.LOCAL)
                        .build());
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getClosedChannels_close_initiator_unknown_but_force_close_remote() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, REMOTE_FORCE_CLOSE, INITIATOR_UNKNOWN, INITIATOR_UNKNOWN)
        ));
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(ClosedChannelFixtures.getWithDefaults(new ForceClosedChannelBuilder())
                        .withOpenInitiator(OpenInitiator.UNKNOWN)
                        .withCloseInitiator(CloseInitiator.REMOTE)
                        .build());
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getClosedChannels_force_closed_local() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, LOCAL_FORCE_CLOSE, INITIATOR_LOCAL, INITIATOR_UNKNOWN)
        ));
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSED_CHANNEL_LOCAL);
    }

    @Test
    void getClosedChannels_force_closed_remote() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, REMOTE_FORCE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE)
        ));
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSED_CHANNEL_REMOTE);
    }

    @Test
    void getClosedChannels_force_closed_breach_detected() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, BREACH_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE)
        ));
        assertThat(grpcClosedChannels.getClosedChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSED_CHANNEL_BREACH);
    }

    @Test
    void getClosedChannels_with_zero_channel_id_not_resolved() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, COOPERATIVE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE),
                closedChannel(0, COOPERATIVE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE)
        ));
        assertThat(grpcClosedChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver).resolveFromChannelPoint(CHANNEL_POINT);
    }

    @Test
    void getClosedChannels_with_zero_channel_id_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID_2));
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(0, COOPERATIVE_CLOSE, INITIATOR_UNKNOWN, INITIATOR_UNKNOWN)
        ));
        assertThat(grpcClosedChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL_2);
    }

    @Test
    void getClosedChannels_ignores_abandoned() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, COOPERATIVE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE),
                closedChannelWithType(ABANDONED)
        ));
        assertThat(grpcClosedChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getClosedChannels_ignores_funding_canceled() {
        when(grpcService.getClosedChannels()).thenReturn(List.of(
                closedChannel(CHANNEL_ID_SHORT, COOPERATIVE_CLOSE, INITIATOR_LOCAL, INITIATOR_REMOTE),
                closedChannelWithType(FUNDING_CANCELED)
        ));
        assertThat(grpcClosedChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    private ChannelCloseSummary closedChannel(
            long channelId,
            ChannelCloseSummary.ClosureType closeType,
            Initiator openInitiator,
            Initiator closeInitiator
    ) {
        return ChannelCloseSummary.newBuilder()
                .setChanId(channelId)
                .setRemotePubkey(PUBKEY_2.toString())
                .setCapacity(CAPACITY.satoshis())
                .setChannelPoint(CHANNEL_POINT.toString())
                .setClosingTxHash(TRANSACTION_HASH_2)
                .setCloseType(closeType)
                .setOpenInitiator(openInitiator)
                .setCloseInitiator(closeInitiator)
                .build();
    }

    private ChannelCloseSummary closedChannelWithType(ChannelCloseSummary.ClosureType abandoned) {
        return ChannelCloseSummary.newBuilder()
                .setChanId(0)
                .setRemotePubkey(PUBKEY_2.toString())
                .setCapacity(CAPACITY.satoshis())
                .setChannelPoint(CHANNEL_POINT.toString())
                .setCloseType(abandoned)
                .build();
    }
}