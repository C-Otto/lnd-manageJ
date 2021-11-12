package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import lnrpc.RoutingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcFeesTest {
    private static final Coins BASE_FEE = Coins.ofMilliSatoshis(1L);
    private static final long FEE_RATE = 123L;

    @InjectMocks
    private GrpcFees grpcFees;

    @Mock
    private GrpcChannelPolicy grpcChannelPolicy;

    @Test
    void getOutgoingFeeRate() {
        when(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).contains(FEE_RATE);
    }

    @Test
    void getOutgoingFeeRate_empty() {
        assertThat(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getIncomingFeeRate() {
        when(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getIncomingFeeRate(CHANNEL_ID)).contains(FEE_RATE);
    }

    @Test
    void getIncomingFeeRate_empty() {
        assertThat(grpcFees.getIncomingFeeRate(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getOutgoingBaseFee() {
        when(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getOutgoingBaseFee(CHANNEL_ID)).contains(BASE_FEE);
    }

    @Test
    void getOutgoingBaseFee_empty() {
        assertThat(grpcFees.getOutgoingBaseFee(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getIncomingBaseFee() {
        when(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getIncomingBaseFee(CHANNEL_ID)).contains(BASE_FEE);
    }

    @Test
    void getIncomingBaseFee_empty() {
        assertThat(grpcFees.getIncomingBaseFee(CHANNEL_ID)).isEmpty();
    }

    private RoutingPolicy routingPolicy() {
        return RoutingPolicy.newBuilder()
                .setFeeRateMilliMsat(FEE_RATE)
                .setFeeBaseMsat(BASE_FEE.milliSatoshis())
                .build();
    }

}