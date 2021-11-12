package de.cotto.lndmanagej.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ChannelService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final GrpcChannels grpcChannels;
    private final LoadingCache<String, Set<LocalChannel>> channelsCache;

    public ChannelService(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
        channelsCache = initializeChannelsCache();
    }

    public Set<LocalChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Set<LocalChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    private LoadingCache<String, Set<LocalChannel>> initializeChannelsCache() {
        CacheLoader<String, Set<LocalChannel>> loader = new CacheLoader<>() {
            @Nonnull
            @Override
            public Set<LocalChannel> load(@Nonnull String ignored) {
                return grpcChannels.getChannels();
            }
        };
        return CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(loader);
    }
}
