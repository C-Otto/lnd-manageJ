package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.ChannelId;
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
}
