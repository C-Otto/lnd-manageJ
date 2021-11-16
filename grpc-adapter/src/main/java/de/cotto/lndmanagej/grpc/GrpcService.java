package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChanInfoRequest;
import lnrpc.Channel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelEdge;
import lnrpc.ClosedChannelsRequest;
import lnrpc.GetInfoResponse;
import lnrpc.LightningGrpc;
import lnrpc.ListChannelsRequest;
import lnrpc.NodeInfo;
import lnrpc.NodeInfoRequest;
import lnrpc.PendingChannelsRequest;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.ForceClosedChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class GrpcService extends GrpcBase {
    private static final int CACHE_EXPIRY_MILLISECONDS = 200;

    private final Metrics metrics;
    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final LoadingCache<Object, List<Channel>> channelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::getChannelsWithoutCache);
    private final LoadingCache<Object, Optional<PendingChannelsResponse>> pendingChannelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::getPendingChannelsWithoutCache);

    public GrpcService(LndConfiguration lndConfiguration, Metrics metrics) throws IOException {
        super(lndConfiguration);
        this.metrics = metrics;
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

    public Optional<NodeInfo> getNodeInfo(Pubkey pubkey) {
        mark("getNodeInfo");
        return get(() -> lightningStub.getNodeInfo(NodeInfoRequest.newBuilder().setPubKey(pubkey.toString()).build()));
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

    private Optional<PendingChannelsResponse> getPendingChannelsWithoutCache() {
        mark("pendingChannels");
        return get(() -> lightningStub.pendingChannels(PendingChannelsRequest.getDefaultInstance()));
    }

    private List<Channel> getChannelsWithoutCache() {
        mark("listChannels");
        return get(() -> lightningStub.listChannels(ListChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    private void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
