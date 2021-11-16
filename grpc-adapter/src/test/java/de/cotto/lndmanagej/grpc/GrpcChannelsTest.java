package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import lnrpc.Channel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelConstraints;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.ForceClosedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static lnrpc.ChannelCloseSummary.ClosureType.ABANDONED;
import static lnrpc.ChannelCloseSummary.ClosureType.FUNDING_CANCELED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelsTest {
    @InjectMocks
    private GrpcChannels grpcChannels;

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
    void getChannels_no_channels() {
        assertThat(grpcChannels.getChannels()).isEmpty();
    }

    @Test
    void getChannels() {
        when(grpcService.getChannels()).thenReturn(List.of(channel(CHANNEL_ID), channel(CHANNEL_ID_2)));
        assertThat(grpcChannels.getChannels()).containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
    }

    @Test
    void getClosedChannels_empty() {
        assertThat(grpcChannels.getClosedChannels()).isEmpty();
    }

    @Test
    void getClosedChannels() {
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(closedChannel(CHANNEL_ID.getShortChannelId()), closedChannel(CHANNEL_ID_2.getShortChannelId()))
        );
        assertThat(grpcChannels.getClosedChannels())
                .containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_2);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getForceClosingChannels_both_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT_2)).thenReturn(Optional.of(CHANNEL_ID_2));
        when(grpcService.getForceClosingChannels()).thenReturn(
                List.of(forceClosingChannel(CHANNEL_POINT), forceClosingChannel(CHANNEL_POINT_2))
        );
        assertThat(grpcChannels.getForceClosingChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2);
    }

    @Test
    void getForceClosingChannels_just_one_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(grpcService.getForceClosingChannels()).thenReturn(
                List.of(forceClosingChannel(CHANNEL_POINT), forceClosingChannel(CHANNEL_POINT_3))
        );
        assertThat(grpcChannels.getForceClosingChannels()).containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL);
    }

    @Test
    void getWaitingCloseChannels_both_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT_2)).thenReturn(Optional.of(CHANNEL_ID_2));
        when(grpcService.getWaitingCloseChannels()).thenReturn(
                List.of(waitingCloseChannel(CHANNEL_POINT), waitingCloseChannel(CHANNEL_POINT_2))
        );
        assertThat(grpcChannels.getWaitingCloseChannels())
                .containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2);
    }

    @Test
    void getWaitingCloseChannels_just_one_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(grpcService.getWaitingCloseChannels()).thenReturn(
                List.of(waitingCloseChannel(CHANNEL_POINT), waitingCloseChannel(CHANNEL_POINT_3))
        );
        assertThat(grpcChannels.getWaitingCloseChannels()).containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL);
    }

    @Test
    void getClosedChannels_with_zero_channel_id_not_resolved() {
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(closedChannel(CHANNEL_ID.getShortChannelId()), closedChannel(0))
        );
        assertThat(grpcChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver).resolveFromChannelPoint(CHANNEL_POINT);
    }

    @Test
    void getClosedChannels_with_zero_channel_id_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID_3));
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(closedChannel(CHANNEL_ID.getShortChannelId()), closedChannel(0))
        );
        assertThat(grpcChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL, CLOSED_CHANNEL_3);
    }

    @Test
    void getClosedChannels_ignores_abandoned() {
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(closedChannel(CHANNEL_ID.getShortChannelId()), closedChannelWithType(ABANDONED))
        );
        assertThat(grpcChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getClosedChannels_ignores_funding_canceled() {
        when(grpcService.getClosedChannels()).thenReturn(
                List.of(closedChannel(CHANNEL_ID.getShortChannelId()), closedChannelWithType(FUNDING_CANCELED))
        );
        assertThat(grpcChannels.getClosedChannels()).containsExactlyInAnyOrder(CLOSED_CHANNEL);
        verify(channelIdResolver, never()).resolveFromChannelPoint(any());
    }

    @Test
    void getChannel() {
        when(grpcService.getChannels()).thenReturn(List.of(channel(CHANNEL_ID_2), channel(CHANNEL_ID)));
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).contains(LOCAL_OPEN_CHANNEL);
    }

    @Test
    void getChannel_empty() {
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).isEmpty();
    }

    private Channel channel(ChannelId channelId) {
        ChannelConstraints localConstraints = ChannelConstraints.newBuilder()
                .setChanReserveSat(BALANCE_INFORMATION.localReserve().satoshis())
                .build();
        ChannelConstraints remoteConstraints = ChannelConstraints.newBuilder()
                .setChanReserveSat(BALANCE_INFORMATION.remoteReserve().satoshis())
                .build();
        return Channel.newBuilder()
                .setChanId(channelId.getShortChannelId())
                .setCapacity(CAPACITY.satoshis())
                .setRemotePubkey(PUBKEY_2.toString())
                .setChannelPoint(CHANNEL_POINT.toString())
                .setLocalBalance(BALANCE_INFORMATION.localBalance().satoshis())
                .setRemoteBalance(BALANCE_INFORMATION.remoteBalance().satoshis())
                .setLocalConstraints(localConstraints)
                .setRemoteConstraints(remoteConstraints)
                .build();
    }

    private ChannelCloseSummary closedChannel(long channelId) {
        return ChannelCloseSummary.newBuilder()
                .setChanId(channelId)
                .setRemotePubkey(PUBKEY_2.toString())
                .setCapacity(CAPACITY.satoshis())
                .setChannelPoint(CHANNEL_POINT.toString())
                .setClosingTxHash(TRANSACTION_HASH_2)
                .build();
    }

    private ForceClosedChannel forceClosingChannel(ChannelPoint channelPoint) {
        return ForceClosedChannel.newBuilder()
                .setChannel(pendingChannel(channelPoint))
                .setClosingTxid(TRANSACTION_HASH_3)
                .build();
    }

    private PendingChannelsResponse.WaitingCloseChannel waitingCloseChannel(ChannelPoint channelPoint) {
        return PendingChannelsResponse.WaitingCloseChannel.newBuilder()
                .setChannel(pendingChannel(channelPoint))
                .build();
    }

    private PendingChannelsResponse.PendingChannel pendingChannel(ChannelPoint channelPoint) {
        return PendingChannelsResponse.PendingChannel.newBuilder()
                .setRemoteNodePub(PUBKEY_2.toString())
                .setCapacity(CAPACITY.satoshis())
                .setChannelPoint(channelPoint.toString())
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