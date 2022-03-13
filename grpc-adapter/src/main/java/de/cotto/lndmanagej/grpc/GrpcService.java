package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lnrpc.ChanInfoRequest;
import lnrpc.Channel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelEdge;
import lnrpc.ChannelGraph;
import lnrpc.ChannelGraphRequest;
import lnrpc.ClosedChannelsRequest;
import lnrpc.ForwardingHistoryRequest;
import lnrpc.ForwardingHistoryResponse;
import lnrpc.GetInfoResponse;
import lnrpc.GetTransactionsRequest;
import lnrpc.Invoice;
import lnrpc.InvoiceSubscription;
import lnrpc.LightningGrpc;
import lnrpc.ListChannelsRequest;
import lnrpc.ListInvoiceRequest;
import lnrpc.ListInvoiceResponse;
import lnrpc.ListPaymentsRequest;
import lnrpc.ListPaymentsResponse;
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
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
@SuppressWarnings("PMD.ExcessiveImports")
public class GrpcService extends GrpcBase {
    private static final Duration CHANNELS_CACHE_REFRESH = Duration.ofMillis(100);
    private static final Duration CHANNELS_CACHE_EXPIRY = Duration.ofMillis(200);

    private static final Duration PENDING_CHANNELS_CACHE_REFRESH = Duration.ofSeconds(5);
    private static final Duration PENDING_CHANNELS_CACHE_EXPIRY = Duration.ofSeconds(10);

    private static final Duration LIST_PEERS_CACHE_REFRESH = Duration.ofSeconds(5);
    private static final Duration LIST_PEERS_CACHE_EXPIRY = Duration.ofSeconds(10);

    private static final Duration TRANSACTIONS_CACHE_REFRESH = Duration.ofSeconds(15);
    private static final Duration TRANSACTIONS_CACHE_EXPIRY = Duration.ofSeconds(30);

    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final LoadingCache<Object, List<Channel>> channelsCache = new CacheBuilder()
            .withRefresh(CHANNELS_CACHE_REFRESH)
            .withExpiry(CHANNELS_CACHE_EXPIRY)
            .build(this::getChannelsWithoutCache);
    private final LoadingCache<Object, Optional<PendingChannelsResponse>> pendingChannelsCache = new CacheBuilder()
            .withRefresh(PENDING_CHANNELS_CACHE_REFRESH)
            .withExpiry(PENDING_CHANNELS_CACHE_EXPIRY)
            .build(this::getPendingChannelsWithoutCache);
    private final LoadingCache<Object, List<Peer>> listPeersCache = new CacheBuilder()
            .withRefresh(LIST_PEERS_CACHE_REFRESH)
            .withExpiry(LIST_PEERS_CACHE_EXPIRY)
            .build(this::listPeersWithoutCache);
    private final LoadingCache<Object, Optional<List<Transaction>>> getTransactionsCache = new CacheBuilder()
            .withRefresh(TRANSACTIONS_CACHE_REFRESH)
            .withExpiry(TRANSACTIONS_CACHE_EXPIRY)
            .build(this::getTransactionsWithoutCache);

    public GrpcService(LndConfiguration lndConfiguration) throws IOException {
        super(lndConfiguration);
        lightningStub = stubCreator.getLightningStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    @Timed
    public Optional<GetInfoResponse> getInfo() {
        return get(() -> lightningStub.getInfo(lnrpc.GetInfoRequest.getDefaultInstance()));
    }

    @Timed
    public List<Peer> listPeers() {
        return listPeersCache.get("");
    }

    @Timed
    public Optional<NodeInfo> getNodeInfo(Pubkey pubkey) {
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

    @Timed
    public Optional<ChannelEdge> getChannelEdge(ChannelId channelId) {
        ChanInfoRequest build = ChanInfoRequest.newBuilder().setChanId(channelId.getShortChannelId()).build();
        return get(() -> lightningStub.getChanInfo(build));
    }

    @Timed
    public List<Channel> getChannels() {
        return channelsCache.get("");
    }

    @Timed
    public List<ChannelCloseSummary> getClosedChannels() {
        return get(() -> lightningStub.closedChannels(ClosedChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    @Timed
    public List<ForceClosedChannel> getForceClosingChannels() {
        return getPendingChannels()
                .map(PendingChannelsResponse::getPendingForceClosingChannelsList)
                .orElse(List.of());
    }

    @Timed
    public List<PendingChannelsResponse.WaitingCloseChannel> getWaitingCloseChannels() {
        return getPendingChannels()
                .map(PendingChannelsResponse::getWaitingCloseChannelsList)
                .orElse(List.of());
    }

    @Timed
    public Optional<List<Transaction>> getTransactions() {
        return getTransactionsCache.get("");
    }

    @Timed
    public Optional<ForwardingHistoryResponse> getForwardingHistory(int offset, int limit) {
        ForwardingHistoryRequest request = ForwardingHistoryRequest.newBuilder()
                .setStartTime(0)
                .setIndexOffset(offset)
                .setNumMaxEvents(limit)
                .build();
        return get(() -> lightningStub.forwardingHistory(request));
    }

    @Timed
    public Optional<ListInvoiceResponse> getInvoices(long offset, int limit) {
        ListInvoiceRequest request = ListInvoiceRequest.newBuilder()
                .setIndexOffset(offset)
                .setNumMaxInvoices(limit)
                .build();
        return get(() -> lightningStub.listInvoices(request));
    }

    public Optional<ListPaymentsResponse> getPayments(long offset, int limit) {
        ListPaymentsRequest request = ListPaymentsRequest.newBuilder()
                .setIndexOffset(offset)
                .setMaxPayments(limit)
                .setIncludeIncomplete(false)
                .setReversed(false)
                .build();
        return get(() -> lightningStub.listPayments(request));
    }

    @Timed
    public Optional<Iterator<Invoice>> subscribeToSettledInvoices(long settleIndex) {
        return get(() -> lightningStub.subscribeInvoices(InvoiceSubscription.newBuilder()
                .setSettleIndex(settleIndex)
                .build()));
    }

    @Timed
    public Optional<ChannelGraph> describeGraph() {
        ChannelGraphRequest request = ChannelGraphRequest.newBuilder()
                .setIncludeUnannounced(true)
                .build();
        return get(() -> lightningStub.describeGraph(request));
    }

    private Optional<List<Transaction>> getTransactionsWithoutCache() {
        return get(
                () -> lightningStub.getTransactions(GetTransactionsRequest.getDefaultInstance()).getTransactionsList()
        );
    }

    private Optional<PendingChannelsResponse> getPendingChannelsWithoutCache() {
        return get(() -> lightningStub.pendingChannels(PendingChannelsRequest.getDefaultInstance()));
    }

    private List<Channel> getChannelsWithoutCache() {
        return get(() -> lightningStub.listChannels(ListChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    private List<Peer> listPeersWithoutCache() {
        return get(() -> lightningStub.listPeers(ListPeersRequest.getDefaultInstance()).getPeersList())
                .orElse(List.of());
    }

    private Optional<PendingChannelsResponse> getPendingChannels() {
        return pendingChannelsCache.get("");
    }
}
