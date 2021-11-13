package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.metrics.MetricsBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChanInfoRequest;
import lnrpc.Channel;
import lnrpc.ChannelEdge;
import lnrpc.GetInfoResponse;
import lnrpc.LightningGrpc;
import lnrpc.ListChannelsRequest;
import lnrpc.NodeInfo;
import lnrpc.NodeInfoRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class GrpcService extends GrpcBase {
    private static final int CACHE_EXPIRY_MILLISECONDS = 200;

    private final Map<String, Meter> meters;

    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final LoadingCache<Object, List<Channel>> channelsCache = new CacheBuilder()
            .withExpiryMilliseconds(CACHE_EXPIRY_MILLISECONDS)
            .build(this::getChannelsWithoutCache);

    public GrpcService(LndConfiguration lndConfiguration, MetricsBuilder metricsBuilder) throws IOException {
        super(lndConfiguration);
        lightningStub = stubCreator.getLightningStub();
        meters = createMeters(metricsBuilder,
                "getInfo", "subscribeHtlcEvents", "getNodeInfo", "getChanInfo", "listChannels"
        );
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
        ChanInfoRequest build = ChanInfoRequest.newBuilder().setChanId(channelId.shortChannelId()).build();
        return get(() -> lightningStub.getChanInfo(build));
    }

    public List<Channel> getChannels() {
        return channelsCache.getUnchecked("");
    }

    private List<Channel> getChannelsWithoutCache() {
        mark("listChannels");
        return get(() -> lightningStub.listChannels(ListChannelsRequest.getDefaultInstance()).getChannelsList())
                .orElse(List.of());
    }

    @VisibleForTesting
    protected void mark(String name) {
        Objects.requireNonNull(meters.get(name)).mark();
    }

    private Map<String, Meter> createMeters(MetricsBuilder metricsBuilder, String... names) {
        LinkedHashMap<String, Meter> result = new LinkedHashMap<>();
        for (String name : names) {
            result.put(name, metricsBuilder.getMetric(MetricRegistry.name(getClass(), name)));
        }
        return result;
    }
}
