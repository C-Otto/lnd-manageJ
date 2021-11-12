package de.cotto.lndmanagej.grpc;

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
    @InjectMocks
    private GrpcFees grpcFees;

    @Mock
    private GrpcChannelPolicy grpcChannelPolicy;

    @Test
    void getOutgoingFeeRate() {
        when(grpcChannelPolicy.getLocalPolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).contains(123L);
    }

    @Test
    void getOutgoingFeeRate_empty() {
        assertThat(grpcFees.getOutgoingFeeRate(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getIncomingFeeRate() {
        when(grpcChannelPolicy.getRemotePolicy(CHANNEL_ID)).thenReturn(Optional.of(routingPolicy()));
        assertThat(grpcFees.getIncomingFeeRate(CHANNEL_ID)).contains(123L);
    }

    @Test
    void getIncomingFeeRate_empty() {
        assertThat(grpcFees.getIncomingFeeRate(CHANNEL_ID)).isEmpty();
    }

    private RoutingPolicy routingPolicy() {
        return RoutingPolicy.newBuilder().setFeeRateMilliMsat(123).build();
    }

}