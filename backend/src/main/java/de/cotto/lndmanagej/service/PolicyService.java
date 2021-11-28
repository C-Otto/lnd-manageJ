package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Policy;
import org.springframework.stereotype.Component;

@Component
public class PolicyService {
    private final GrpcFees grpcFees;

    public PolicyService(GrpcFees grpcFees) {
        this.grpcFees = grpcFees;
    }

    public Policies getPolicies(ChannelId channelId) {
        return new Policies(
                new Policy(getOutgoingFeeRate(channelId), getOutgoingBaseFee(channelId), isEnabledLocal(channelId)),
                new Policy(getIncomingFeeRate(channelId), getIncomingBaseFee(channelId), isEnabledRemote(channelId))
        );
    }

    private long getIncomingFeeRate(ChannelId channelId) {
        return grpcFees.getIncomingFeeRate(channelId).orElseThrow(IllegalStateException::new);
    }

    private long getOutgoingFeeRate(ChannelId channelId) {
        return grpcFees.getOutgoingFeeRate(channelId).orElseThrow(IllegalStateException::new);
    }

    private Coins getOutgoingBaseFee(ChannelId channelId) {
        return grpcFees.getOutgoingBaseFee(channelId).orElseThrow(IllegalStateException::new);
    }

    private Coins getIncomingBaseFee(ChannelId channelId) {
        return grpcFees.getIncomingBaseFee(channelId).orElseThrow(IllegalStateException::new);
    }

    private boolean isEnabledLocal(ChannelId channelId) {
        return grpcFees.isEnabledLocal(channelId).orElseThrow(IllegalStateException::new);
    }

    private boolean isEnabledRemote(ChannelId channelId) {
        return grpcFees.isEnabledRemote(channelId).orElseThrow(IllegalStateException::new);
    }
}
