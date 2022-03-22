package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelEdge;
import lnrpc.RoutingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelPolicyTest {
    private static final int FEE_RATE_FIRST = 123;
    private static final int FEE_RATE_SECOND = 456;

    @InjectMocks
    private GrpcChannelPolicy grpcChannelPolicy;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
    }

    @Test
    void getLocalPolicy_local_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY, PUBKEY_2)));
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).contains(routingPolicy(FEE_RATE_FIRST));
    }

    @Test
    void getLocalPolicy_local_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY)));
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).contains(routingPolicy(FEE_RATE_SECOND));
    }

    @Test
    void getLocalPolicy_not_local() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getLocalPolicy_empty() {
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getRemotePolicy_local_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY, PUBKEY_2)));
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).contains(routingPolicy(FEE_RATE_SECOND));
    }

    @Test
    void getRemotePolicy_local_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY)));
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).contains(routingPolicy(FEE_RATE_FIRST));
    }

    @Test
    void getRemotePolicy_not_local() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getPolicyFrom_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_2)).contains(routingPolicy(FEE_RATE_FIRST));
    }

    @Test
    void getPolicyFrom_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_3)).contains(routingPolicy(FEE_RATE_SECOND));
    }

    @Test
    void getPolicyFrom_neither() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_4)).isEmpty();
    }

    @Test
    void getPolicyTo_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_3)).contains(routingPolicy(FEE_RATE_FIRST));
    }

    @Test
    void getPolicyTo_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_2)).contains(routingPolicy(FEE_RATE_SECOND));
    }

    @Test
    void getPolicyTo_neither() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_4)).isEmpty();
    }

    @Test
    void getRemotePolicy_empty() {
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).isEmpty();
    }

    private ChannelEdge channelEdge(Pubkey firstPubkey, Pubkey secondPubkey) {
        return ChannelEdge.newBuilder()
                .setNode1Pub(firstPubkey.toString())
                .setNode2Pub(secondPubkey.toString())
                .setNode1Policy(routingPolicy(FEE_RATE_FIRST))
                .setNode2Policy(routingPolicy(FEE_RATE_SECOND))
                .build();
    }

    private RoutingPolicy routingPolicy(int feeRate) {
        return RoutingPolicy.newBuilder().setFeeRateMilliMsat(feeRate).build();
    }
}
