package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PolicyService {
    private final GrpcChannelPolicy grpcChannelPolicy;

    public PolicyService(GrpcChannelPolicy grpcChannelPolicy) {
        this.grpcChannelPolicy = grpcChannelPolicy;
    }

    @Timed
    public PoliciesForLocalChannel getPolicies(LocalChannel localChannel) {
        ChannelId channelId = localChannel.getId();
        return new PoliciesForLocalChannel(
                grpcChannelPolicy.getLocalPolicy(channelId).orElseThrow(IllegalStateException::new),
                grpcChannelPolicy.getRemotePolicy(channelId).orElseThrow(IllegalStateException::new)
        );
    }

    public Optional<Policy> getPolicyFrom(ChannelId channelId, Pubkey pubkey) {
        return grpcChannelPolicy.getPolicyFrom(channelId, pubkey);
    }

    public Optional<Policy> getPolicyTo(ChannelId channelId, Pubkey pubkey) {
        return grpcChannelPolicy.getPolicyTo(channelId, pubkey);
    }
}
