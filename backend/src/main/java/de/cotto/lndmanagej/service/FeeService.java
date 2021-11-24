package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeConfiguration;
import org.springframework.stereotype.Component;

@Component
public class FeeService {
    private final GrpcFees grpcFees;

    public FeeService(GrpcFees grpcFees) {
        this.grpcFees = grpcFees;
    }

    public long getIncomingFeeRate(ChannelId channelId) {
        return grpcFees.getIncomingFeeRate(channelId).orElseThrow(IllegalStateException::new);
    }

    public long getOutgoingFeeRate(ChannelId channelId) {
        return grpcFees.getOutgoingFeeRate(channelId).orElseThrow(IllegalStateException::new);
    }

    public Coins getOutgoingBaseFee(ChannelId channelId) {
        return grpcFees.getOutgoingBaseFee(channelId).orElseThrow(IllegalStateException::new);
    }

    public Coins getIncomingBaseFee(ChannelId channelId) {
        return grpcFees.getIncomingBaseFee(channelId).orElseThrow(IllegalStateException::new);
    }

    public FeeConfiguration getFeeConfiguration(ChannelId channelId) {
        return new FeeConfiguration(
                getOutgoingFeeRate(channelId),
                getOutgoingBaseFee(channelId),
                getIncomingFeeRate(channelId),
                getIncomingBaseFee(channelId)
        );
    }
}
