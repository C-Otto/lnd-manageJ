package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import lnrpc.Channel;
import lnrpc.ChannelConstraints;
import lnrpc.Initiator;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.ForceClosedChannel;
import lnrpc.PendingHTLC;
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
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.HTLC_OUTPOINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
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
        when(grpcService.getChannels()).thenReturn(List.of(
                channel(CHANNEL_ID, true, false),
                channel(CHANNEL_ID_2, false, false)
        ));
        assertThat(grpcChannels.getChannels()).containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2);
    }

    @Test
    void getChannels_private() {
        when(grpcService.getChannels()).thenReturn(List.of(
                channel(CHANNEL_ID, true, true)
        ));
        assertThat(grpcChannels.getChannels()).containsExactlyInAnyOrder(LOCAL_OPEN_CHANNEL_PRIVATE);
    }

    @Test
    void getForceClosingChannels_both_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT_2)).thenReturn(Optional.of(CHANNEL_ID_2));
        when(grpcService.getForceClosingChannels()).thenReturn(List.of(
                forceClosingChannel(CHANNEL_POINT, Initiator.INITIATOR_LOCAL),
                forceClosingChannel(CHANNEL_POINT_2, Initiator.INITIATOR_REMOTE)
        ));
        assertThat(grpcChannels.getForceClosingChannels())
                .containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2);
    }

    @Test
    void getForceClosingChannels_just_one_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(grpcService.getForceClosingChannels()).thenReturn(List.of(
                forceClosingChannel(CHANNEL_POINT, Initiator.INITIATOR_LOCAL),
                forceClosingChannel(CHANNEL_POINT_3, Initiator.INITIATOR_LOCAL)
        ));
        assertThat(grpcChannels.getForceClosingChannels()).containsExactlyInAnyOrder(FORCE_CLOSING_CHANNEL);
    }

    @Test
    void getWaitingCloseChannels_both_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT_2)).thenReturn(Optional.of(CHANNEL_ID_2));
        when(grpcService.getWaitingCloseChannels()).thenReturn(List.of(
                waitingCloseChannel(CHANNEL_POINT, Initiator.INITIATOR_LOCAL),
                waitingCloseChannel(CHANNEL_POINT_2, Initiator.INITIATOR_REMOTE)
        ));
        assertThat(grpcChannels.getWaitingCloseChannels())
                .containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2);
    }

    @Test
    void getWaitingCloseChannels_just_one_resolved() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        when(grpcService.getWaitingCloseChannels()).thenReturn(List.of(
                waitingCloseChannel(CHANNEL_POINT, Initiator.INITIATOR_LOCAL),
                waitingCloseChannel(CHANNEL_POINT_3, Initiator.INITIATOR_REMOTE)
        ));
        assertThat(grpcChannels.getWaitingCloseChannels()).containsExactlyInAnyOrder(WAITING_CLOSE_CHANNEL);
    }

    @Test
    void getChannel() {
        when(grpcService.getChannels()).thenReturn(List.of(
                channel(CHANNEL_ID_2, false, false),
                channel(CHANNEL_ID, true, false)
        ));
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).contains(LOCAL_OPEN_CHANNEL);
    }

    @Test
    void getChannel_empty() {
        assertThat(grpcChannels.getChannel(CHANNEL_ID)).isEmpty();
    }

    private Channel channel(ChannelId channelId, boolean isInitiator, boolean isPrivate) {
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
                .setInitiator(isInitiator)
                .setPrivate(isPrivate)
                .build();
    }

    private ForceClosedChannel forceClosingChannel(ChannelPoint channelPoint, Initiator initiator) {
        return ForceClosedChannel.newBuilder()
                .setChannel(pendingChannel(channelPoint, initiator))
                .setClosingTxid(TRANSACTION_HASH_3)
                .addPendingHtlcs(PendingHTLC.newBuilder().setOutpoint(HTLC_OUTPOINT.toString()).build())
                .build();
    }

    private PendingChannelsResponse.WaitingCloseChannel waitingCloseChannel(
            ChannelPoint channelPoint,
            Initiator initiator
    ) {
        return PendingChannelsResponse.WaitingCloseChannel.newBuilder()
                .setChannel(pendingChannel(channelPoint, initiator))
                .build();
    }

    private PendingChannelsResponse.PendingChannel pendingChannel(ChannelPoint channelPoint, Initiator initiator) {
        return PendingChannelsResponse.PendingChannel.newBuilder()
                .setRemoteNodePub(PUBKEY_2.toString())
                .setCapacity(CAPACITY.satoshis())
                .setChannelPoint(channelPoint.toString())
                .setInitiator(initiator)
                .build();
    }
}