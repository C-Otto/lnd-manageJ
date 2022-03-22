package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.grpc.GrpcFees;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import org.springframework.stereotype.Component;

@Component
public class PolicyService {
    private final GrpcFees grpcFees;

    public PolicyService(GrpcFees grpcFees) {
        this.grpcFees = grpcFees;
    }

    @Timed
    public PoliciesForLocalChannel getPolicies(LocalChannel localChannel) {
        ChannelId channelId = localChannel.getId();
        return new PoliciesForLocalChannel(
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
