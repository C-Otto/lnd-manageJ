package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.PrivateResolver;
import lnrpc.Initiator;

import java.util.Optional;

public class GrpcChannelsBase {
    private final ChannelIdResolver channelIdResolver;
    private final PrivateResolver privateResolver;

    protected GrpcChannelsBase(ChannelIdResolver channelIdResolver, PrivateResolver privateResolver) {
        this.channelIdResolver = channelIdResolver;
        this.privateResolver = privateResolver;
    }

    OpenInitiator getOpenInitiator(Initiator openInitiator) {
        return switch (openInitiator) {
            case INITIATOR_LOCAL -> OpenInitiator.LOCAL;
            case INITIATOR_REMOTE -> OpenInitiator.REMOTE;
            case INITIATOR_UNKNOWN -> OpenInitiator.UNKNOWN;
            case null, default -> throw new IllegalStateException("unexpected open initiator: " + openInitiator);
        };
    }

    Optional<ChannelId> resolveChannelId(ChannelPoint channelPoint) {
        return channelIdResolver.resolveFromChannelPoint(channelPoint);
    }

    boolean resolveIsPrivate(ChannelId channelId) {
        return privateResolver.isPrivate(channelId);
    }
}
