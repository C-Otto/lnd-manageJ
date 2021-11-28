package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GrpcFees {
    private final GrpcChannelPolicy grpcChannelPolicy;

    public GrpcFees(GrpcChannelPolicy grpcChannelPolicy) {
        this.grpcChannelPolicy = grpcChannelPolicy;
    }

    public Optional<Long> getOutgoingFeeRate(ChannelId channelId) {
        return grpcChannelPolicy.getLocalPolicy(channelId).map(RoutingPolicy::getFeeRateMilliMsat);
    }

    public Optional<Long> getIncomingFeeRate(ChannelId channelId) {
        return grpcChannelPolicy.getRemotePolicy(channelId).map(RoutingPolicy::getFeeRateMilliMsat);
    }

    public Optional<Coins> getOutgoingBaseFee(ChannelId channelId) {
        return grpcChannelPolicy.getLocalPolicy(channelId)
                .map(RoutingPolicy::getFeeBaseMsat)
                .map(Coins::ofMilliSatoshis);
    }

    public Optional<Coins> getIncomingBaseFee(ChannelId channelId) {
        return grpcChannelPolicy.getRemotePolicy(channelId)
                .map(RoutingPolicy::getFeeBaseMsat)
                .map(Coins::ofMilliSatoshis);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isEnabledLocal(ChannelId channelId) {
        return grpcChannelPolicy.getLocalPolicy(channelId)
                .map(RoutingPolicy::getDisabled)
                .map(b -> !b);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isEnabledRemote(ChannelId channelId) {
        return grpcChannelPolicy.getRemotePolicy(channelId)
                .map(RoutingPolicy::getDisabled)
                .map(b -> !b);
    }
}
