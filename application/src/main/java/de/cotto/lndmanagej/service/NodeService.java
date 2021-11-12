package de.cotto.lndmanagej.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class NodeService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 30;

    private final GrpcNodeInfo grpcNodeInfo;
    private final ChannelService channelService;
    private final LoadingCache<Pubkey, String> cache;

    public NodeService(GrpcNodeInfo grpcNodeInfo, ChannelService channelService) {
        this.grpcNodeInfo = grpcNodeInfo;
        this.channelService = channelService;
        CacheLoader<Pubkey, String> loader = new CacheLoader<>() {
            @Nonnull
            @Override
            public String load(@Nonnull Pubkey pubkey) {
                return getNode(pubkey).alias();
            }
        };
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(loader);
    }

    public List<ChannelId> getOpenChannelIds(Pubkey pubkey) {
        Node node = getNode(pubkey);
        return channelService.getOpenChannelsWith(node).stream()
                .map(Channel::getId)
                .sorted()
                .collect(Collectors.toList());
    }

    public String getAlias(Pubkey pubkey) {
        return cache.getUnchecked(pubkey);
    }

    private Node getNode(Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey);
    }
}
