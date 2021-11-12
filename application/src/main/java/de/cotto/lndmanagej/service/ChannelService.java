package de.cotto.lndmanagej.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.grpc.GrpcChannels;
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
public class ChannelService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 5;

    private final GrpcChannels grpcChannels;
    private final LoadingCache<Pubkey, List<ChannelId>> channelsWithPeerCache;

    public ChannelService(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
        CacheLoader<Pubkey, List<ChannelId>> loader = new CacheLoader<>() {
            @Nonnull
            @Override
            public List<ChannelId> load(@Nonnull Pubkey peer) {
                return getOpenChannelsWithWithoutCache(peer);
            }
        };
        channelsWithPeerCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(loader);
    }

    public List<ChannelId> getOpenChannelsWith(Pubkey peer) {
        return channelsWithPeerCache.getUnchecked(peer);
    }

    public List<ChannelId> getOpenChannelsWith(Node peer) {
        return getOpenChannelsWith(peer.pubkey());
    }

    public List<ChannelId> getOpenChannelsWithWithoutCache(Pubkey peer) {
        return grpcChannels.getChannels().stream()
                .filter(c -> c.getPubkeys().contains(peer))
                .map(Channel::getId)
                .sorted()
                .collect(Collectors.toList());
    }
}
