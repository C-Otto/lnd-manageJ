package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CltvService {
    private final ChannelService channelService;
    private final GrpcGetInfo grpcGetInfo;
    private final PolicyService policyService;

    public CltvService(ChannelService channelService, GrpcGetInfo grpcGetInfo, PolicyService policyService) {
        this.channelService = channelService;
        this.grpcGetInfo = grpcGetInfo;
        this.policyService = policyService;
    }

    public int getMaximumDeltaForEdges(
            int maximumCltvExpiry,
            int finalCltvDelta,
            Optional<Pubkey> peer,
            Pubkey target
    ) {
        Pubkey peerPubkey = peer.orElse(null);
        if (peerPubkey == null) {
            return maximumCltvExpiry - finalCltvDelta;
        }
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        if (ownPubkey.equals(target)) {
            int maximumCltvDeltaFromPeer = channelService.getOpenChannelsWith(peerPubkey).stream()
                    .map(c -> policyService.getPolicyFrom(c.getId(), peerPubkey))
                    .flatMap(Optional::stream)
                    .mapToInt(Policy::timeLockDelta)
                    .max()
                    .orElse(0);
            return maximumCltvExpiry - finalCltvDelta - maximumCltvDeltaFromPeer;
        }
        return maximumCltvExpiry - finalCltvDelta;
    }
}
