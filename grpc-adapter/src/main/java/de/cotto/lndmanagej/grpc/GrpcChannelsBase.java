package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.PrivateResolver;
import lnrpc.Initiator;

import java.util.Optional;

import static lnrpc.Initiator.INITIATOR_LOCAL;
import static lnrpc.Initiator.INITIATOR_REMOTE;
import static lnrpc.Initiator.INITIATOR_UNKNOWN;

public class GrpcChannelsBase {
    private final ChannelIdResolver channelIdResolver;
    private final PrivateResolver privateResolver;

    protected GrpcChannelsBase(ChannelIdResolver channelIdResolver, PrivateResolver privateResolver) {
        this.channelIdResolver = channelIdResolver;
        this.privateResolver = privateResolver;
    }

    OpenInitiator getOpenInitiator(Initiator openInitiator) {
        if (openInitiator.equals(INITIATOR_LOCAL)) {
            return OpenInitiator.LOCAL;
        } else if (openInitiator.equals(INITIATOR_REMOTE)) {
            return OpenInitiator.REMOTE;
        } else if (openInitiator.equals(INITIATOR_UNKNOWN)) {
            return OpenInitiator.UNKNOWN;
        }
        throw new IllegalStateException("unexpected open initiator: " + openInitiator);
    }

    Optional<ChannelId> resolveChannelId(ChannelPoint channelPoint) {
        return channelIdResolver.resolveFromChannelPoint(channelPoint);
    }

    boolean resolveIsPrivate(ChannelId channelId) {
        return privateResolver.isPrivate(channelId);
    }
}
