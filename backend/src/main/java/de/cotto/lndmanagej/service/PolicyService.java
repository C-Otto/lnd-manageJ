package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.model.Channel;
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
    private final ChannelService channelService;

    public PolicyService(GrpcChannelPolicy grpcChannelPolicy, ChannelService channelService) {
        this.grpcChannelPolicy = grpcChannelPolicy;
        this.channelService = channelService;
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

    public Optional<Long> getMinimumFeeRateFrom(Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .map(id -> getPolicyFrom(id, pubkey))
                .flatMap(Optional::stream)
                .map(Policy::feeRate)
                .min(Long::compare);
    }

    public Optional<Long> getMinimumFeeRateTo(Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .map(id -> getPolicyTo(id, pubkey))
                .flatMap(Optional::stream)
                .map(Policy::feeRate)
                .min(Long::compare);
    }
}
