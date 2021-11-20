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
import lnrpc.Transaction;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@SuppressWarnings("PMD.ExcessiveImports")
public class GrpcService extends GrpcBase {
    private static final int CHANNELS_CACHE_EXPIRY_MS = 200;
    private static final int PENDING_CHANNELS_CACHE_EXPIRY_MS = 10_000;
    private static final int LIST_PEERS_CACHE_EXPIRY_MS = 10_000;
    private static final int TRANSACTIONS_CACHE_EXPIRY_MS = 30_000;

    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final LoadingCache<Object, List<Channel>> channelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CHANNELS_CACHE_EXPIRY_MS)
            .build(this::getChannelsWithoutCache);
    private final LoadingCache<Object, Optional<PendingChannelsResponse>> pendingChannelsCache = new CacheBuilder()
            .withExpiryMilliseconds(PENDING_CHANNELS_CACHE_EXPIRY_MS)
            .build(this::getPendingChannelsWithoutCache);
    private final LoadingCache<Object, List<Peer>> listPeersCache = new CacheBuilder()
            .withExpiryMilliseconds(LIST_PEERS_CACHE_EXPIRY_MS)
            .build(this::listPeersWithoutCache);
    private final LoadingCache<Object, Optional<List<Transaction>>> getTransactionsCache = new CacheBuilder()
            .withExpiryMilliseconds(TRANSACTIONS_CACHE_EXPIRY_MS)
            .build(this::getTransactionsWithoutCache);

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

    public Optional<List<Transaction>> getTransactions() {
        return getTransactionsCache.getUnchecked("");
    }

    private Optional<List<Transaction>> getTransactionsWithoutCache() {
        mark("getTransactions");
        return get(() -> lightningStub.getTransactions(GetTransactionsRequest.getDefaultInstance())
                .getTransactionsList());
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
