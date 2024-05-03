package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
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
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcChannelPolicyTest {
    private static final int FEE_RATE_FIRST = 123;
    private static final int FEE_RATE_SECOND = 456;
    private static final int TIME_LOCK_DELTA = 40;
    private static final Coins MIN_HTLC = Coins.ofMilliSatoshis(159);
    private static final Coins MAX_HTLC = Coins.ofMilliSatoshis(5_432);

    @InjectMocks
    private GrpcChannelPolicy grpcChannelPolicy;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private GrpcPolicy grpcPolicy;

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        lenient().when(grpcPolicy.toPolicy(any())).thenCallRealMethod();
    }

    @Test
    void getLocalPolicy_local_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY, PUBKEY_2)));
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID))
                .contains(new Policy(FEE_RATE_FIRST, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getLocalPolicy_local_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY)));
        assertThat(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID))
                .contains(new Policy(FEE_RATE_SECOND, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
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
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID))
                .contains(new Policy(FEE_RATE_SECOND, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getRemotePolicy_local_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY)));
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID))
                .contains(new Policy(FEE_RATE_FIRST, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getRemotePolicy_not_local() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getPolicyFrom_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_2))
                .contains(new Policy(FEE_RATE_FIRST, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getPolicyFrom_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_3))
                .contains(new Policy(FEE_RATE_SECOND, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getPolicyFrom_neither() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyFrom(CHANNEL_ID, PUBKEY_4)).isEmpty();
    }

    @Test
    void getPolicyTo_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_3))
                .contains(new Policy(FEE_RATE_FIRST, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
    }

    @Test
    void getPolicyTo_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_2))
                .contains(new Policy(FEE_RATE_SECOND, Coins.NONE, true, TIME_LOCK_DELTA, MIN_HTLC, MAX_HTLC));
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

    @Test
    void getOtherPubkey_edge_not_found() {
        assertThat(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID, PUBKEY_4)).isEmpty();
    }

    @Test
    void getOtherPubkey_given_first() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID, PUBKEY_2)).contains(PUBKEY_3);
    }

    @Test
    void getOtherPubkey_given_second() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID, PUBKEY_3)).contains(PUBKEY_2);
    }

    @Test
    void getOtherPubkey_no_matching_pubkey() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(channelEdge(PUBKEY_2, PUBKEY_3)));
        assertThat(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID, PUBKEY_4)).isEmpty();
    }

    @Test
    void getPolicy_with_unknown_custom_records() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(
                edgeWithCustomRecord(123, ByteString.copyFromUtf8("test"))
        ));
        Policy policy = grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_2).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(policy.inboundFeeRate()).isEqualTo(0L);
            softly.assertThat(policy.inboundBaseFee()).isEqualTo(Coins.NONE);
        });
    }

    @Test
    void getPolicy_with_negative_inbound_fees() {
        ByteString inboundBaseFee = ByteString.fromHex(Integer.toHexString(-456));
        ByteString inboundFeeRate = ByteString.fromHex(Integer.toHexString(-123));
        ByteString value = inboundBaseFee.concat(inboundFeeRate);

        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(
                edgeWithCustomRecord(55_555, value)
        ));
        Policy policy = grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_2).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(policy.inboundBaseFee()).isEqualTo(Coins.ofMilliSatoshis(-456));
            softly.assertThat(policy.inboundFeeRate()).isEqualTo(-123L);
        });
    }

    @Test
    void getPolicy_with_negative_inbound_fees_using_hex_string() {
        when(grpcService.getChannelEdge(CHANNEL_ID)).thenReturn(Optional.of(
                edgeWithCustomRecord(55_555, ByteString.fromHex("00000000fffffe0c"))
        ));
        Policy policy = grpcChannelPolicy.getPolicyTo(CHANNEL_ID, PUBKEY_2).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(policy.inboundBaseFee()).isEqualTo(Coins.NONE);
            softly.assertThat(policy.inboundFeeRate()).isEqualTo(-500L);
        });
    }

    private ChannelEdge channelEdge(Pubkey firstPubkey, Pubkey secondPubkey) {
        return ChannelEdge.newBuilder()
                .setNode1Pub(firstPubkey.toString())
                .setNode2Pub(secondPubkey.toString())
                .setNode1Policy(routingPolicy(FEE_RATE_FIRST))
                .setNode2Policy(routingPolicy(FEE_RATE_SECOND))
                .build();
    }

    private ChannelEdge edgeWithCustomRecord(int key, ByteString value) {
        return ChannelEdge.newBuilder()
                .setNode1Pub(PUBKEY.toString())
                .setNode2Pub(PUBKEY_2.toString())
                .setNode1Policy(policyWithCustomRecord(key, value))
                .setNode2Policy(routingPolicy(FEE_RATE_SECOND))
                .build();
    }

    private RoutingPolicy routingPolicy(int feeRate) {
        return RoutingPolicy.newBuilder()
                .setFeeRateMilliMsat(feeRate)
                .setTimeLockDelta(TIME_LOCK_DELTA)
                .setMinHtlc(MIN_HTLC.milliSatoshis())
                .setMaxHtlcMsat(MAX_HTLC.milliSatoshis())
                .build();
    }

    private RoutingPolicy policyWithCustomRecord(long key, ByteString value) {
        return RoutingPolicy.newBuilder()
                .setFeeRateMilliMsat(FEE_RATE_FIRST)
                .setTimeLockDelta(TIME_LOCK_DELTA)
                .setMinHtlc(MIN_HTLC.milliSatoshis())
                .setMaxHtlcMsat(MAX_HTLC.milliSatoshis())
                .putCustomRecords(key, value)
                .build();
    }
}
