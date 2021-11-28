package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BreachForceClosedChannelBuilder;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ClosedChannelBuilder;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoopClosedChannelBuilder;
import de.cotto.lndmanagej.model.ForceClosedChannelBuilder;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.OpenInitiatorResolver;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelCloseSummary.ClosureType;
import lnrpc.Initiator;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lnrpc.ChannelCloseSummary.ClosureType.LOCAL_FORCE_CLOSE;
import static lnrpc.ChannelCloseSummary.ClosureType.REMOTE_FORCE_CLOSE;
import static lnrpc.Initiator.INITIATOR_LOCAL;
import static lnrpc.Initiator.INITIATOR_REMOTE;
import static lnrpc.Initiator.INITIATOR_UNKNOWN;

@Component
public class GrpcClosedChannels extends GrpcChannelsBase {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;
    private final OpenInitiatorResolver openInitiatorResolver;

    public GrpcClosedChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo,
            ChannelIdResolver channelIdResolver,
            OpenInitiatorResolver openInitiatorResolver
    ) {
        super(channelIdResolver);
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
        this.openInitiatorResolver = openInitiatorResolver;
    }

    public Set<ClosedChannel> getClosedChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getClosedChannels().stream()
                .filter(this::hasSupportedCloseType)
                .map(channelCloseSummary -> toClosedChannel(channelCloseSummary, ownPubkey))
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    private boolean hasSupportedCloseType(ChannelCloseSummary channelCloseSummary) {
        ClosureType closeType = channelCloseSummary.getCloseType();
        return closeType != ClosureType.ABANDONED && closeType != ClosureType.FUNDING_CANCELED;
    }

    private Optional<ClosedChannel> toClosedChannel(
            ChannelCloseSummary channelCloseSummary,
            Pubkey ownPubkey
    ) {
        ClosureType closureType = channelCloseSummary.getCloseType();
        CloseInitiator closeInitiator = getCloseInitiator(channelCloseSummary);
        ClosedChannelBuilder<? extends ClosedChannel> builder;
        if (closureType.equals(ClosureType.COOPERATIVE_CLOSE)) {
            builder = new CoopClosedChannelBuilder().withCloseInitiator(closeInitiator);
        } else if (closureType.equals(ClosureType.BREACH_CLOSE)) {
            builder = new BreachForceClosedChannelBuilder();
        } else {
            builder = new ForceClosedChannelBuilder().withCloseInitiator(closeInitiator);
        }
        ChannelPoint channelPoint = ChannelPoint.create(channelCloseSummary.getChannelPoint());
        OpenInitiator openInitiator = getOpenInitiator(
                channelCloseSummary.getOpenInitiator(),
                channelPoint.getTransactionHash()
        );
        return getChannelId(channelCloseSummary.getChanId(), channelPoint)
                .map(channelId -> builder
                        .withChannelId(channelId)
                        .withChannelPoint(channelPoint)
                        .withCapacity(Coins.ofSatoshis(channelCloseSummary.getCapacity()))
                        .withOwnPubkey(ownPubkey)
                        .withRemotePubkey(Pubkey.create(channelCloseSummary.getRemotePubkey()))
                        .withCloseTransactionHash(channelCloseSummary.getClosingTxHash())
                        .withOpenInitiator(openInitiator)
                        .withCloseHeight(channelCloseSummary.getCloseHeight())
                        .build()
                );
    }

    private OpenInitiator getOpenInitiator(Initiator initiator, String transactionHash) {
        OpenInitiator openInitiator = getOpenInitiator(initiator);
        if (openInitiator.equals(OpenInitiator.UNKNOWN)) {
            return openInitiatorResolver.resolveFromOpenTransactionHash(transactionHash);
        }
        return openInitiator;
    }

    private CloseInitiator getCloseInitiator(ChannelCloseSummary channelCloseSummary) {
        Initiator closeInitiator = channelCloseSummary.getCloseInitiator();
        ChannelCloseSummary.ClosureType closureType = channelCloseSummary.getCloseType();
        if (closeInitiator.equals(INITIATOR_LOCAL) || closureType.equals(LOCAL_FORCE_CLOSE)) {
            return CloseInitiator.LOCAL;
        } else if (closeInitiator.equals(INITIATOR_REMOTE) || closureType.equals(REMOTE_FORCE_CLOSE)) {
            return CloseInitiator.REMOTE;
        } else if (closeInitiator.equals(INITIATOR_UNKNOWN)) {
            return CloseInitiator.UNKNOWN;
        }
        throw new IllegalStateException("unexpected close initiator: " + closeInitiator);
    }
}
