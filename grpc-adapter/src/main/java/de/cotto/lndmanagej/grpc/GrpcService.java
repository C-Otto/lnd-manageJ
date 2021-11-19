package de.cotto.lndmanagej.grpc;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lnrpc.ChanInfoRequest;
import lnrpc.Channel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelEdge;
import lnrpc.ClosedChannelsRequest;
import lnrpc.GetInfoResponse;
import lnrpc.GetTransactionsRequest;
import lnrpc.LightningGrpc;
import lnrpc.ListChannelsRequest;
import lnrpc.ListPeersRequest;
import lnrpc.NodeInfo;
import lnrpc.NodeInfoRequest;
import lnrpc.Peer;
import lnrpc.PendingChannelsRequest;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.ForceClosedChannel;
import lnrpc.TransactionDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@SuppressWarnings("PMD.ExcessiveImports")
public class GrpcService extends GrpcBase {
    private static final int CACHE_EXPIRY_MILLISECONDS = 200;

    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final LoadingCache<Object, List<Channel>> channelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::getChannelsWithoutCache);
    private final LoadingCache<Object, Optional<PendingChannelsResponse>> pendingChannelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::getPendingChannelsWithoutCache);
    private final LoadingCache<Object, List<Peer>> listPeersCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::listPeersWithoutCache);

    public GrpcService(LndConfiguration lndConfiguration, Metrics metrics) throws IOException {
        super(lndConfiguration, metrics);
        lightningStub = stubCreator.getLightningStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    Optional<GetInfoResponse> getInfo() {
        mark("getInfo");
        return get(() -> lightningStub.getInfo(lnrpc.GetInfoRequest.getDefaultInstance()));
    }

    public List<Peer> listPeers() {
        return listPeersCache.getUnchecked("");
    }

    private List<Peer> listPeersWithoutCache() {
        mark("listPeers");
        return get(
                () -> lightningStub.listPeers(ListPeersRequest.getDefaultInstance()).getPeersList()
        ).orElse(List.of());
    }

    public Optional<NodeInfo> getNodeInfo(Pubkey pubkey) {
        mark("getNodeInfo");
        return get(() -> {
            try {
                return lightningStub.getNodeInfo(NodeInfoRequest.newBuilder().setPubKey(pubkey.toString()).build());
            } catch (StatusRuntimeException exception) {
                if (Status.Code.NOT_FOUND.equals(exception.getStatus().getCode())) {
                    // ignore
                    return null;
                }
                throw exception;
            }
        });
    }

    public Optional<ChannelEdge> getChannelEdge(ChannelId channelId) {
        mark("getChanInfo");
        ChanInfoRequest build = ChanInfoRequest.newBuilder().setChanId(channelId.getShortChannelId()).build();
        return get(() -> lightningStub.getChanInfo(build));
    }

    public List<Channel> getChannels() {
        return channelsCache.getUnchecked("");
    }

    private Optional<PendingChannelsResponse> getPendingChannels() {
        return pendingChannelsCache.getUnchecked("");
    }

    public List<ChannelCloseSummary> getClosedChannels() {
        mark("closedChannels");
        return get(() -> lightningStub.closedChannels(ClosedChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    public List<ForceClosedChannel> getForceClosingChannels() {
        return getPendingChannels()
                .map(PendingChannelsResponse::getPendingForceClosingChannelsList)
                .orElse(List.of());
    }

    public List<PendingChannelsResponse.WaitingCloseChannel> getWaitingCloseChannels() {
        return getPendingChannels()
                .map(PendingChannelsResponse::getWaitingCloseChannelsList)
                .orElse(List.of());
    }

    public Optional<TransactionDetails> getTransactionsInBlock(int blockHeight) {
        mark("getTransactions");
        GetTransactionsRequest request = GetTransactionsRequest.newBuilder()
                .setStartHeight(blockHeight)
                .setEndHeight(blockHeight)
                .build();
        return get(() -> lightningStub.getTransactions(request));
    }

    private Optional<PendingChannelsResponse> getPendingChannelsWithoutCache() {
        mark("pendingChannels");
        return get(() -> lightningStub.pendingChannels(PendingChannelsRequest.getDefaultInstance()));
    }

    private List<Channel> getChannelsWithoutCache() {
        mark("listChannels");
        return get(() -> lightningStub.listChannels(ListChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }
}
