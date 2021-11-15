package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.UnresolvedClosedChannel;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelService {
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final LoadingCache<Object, Set<LocalOpenChannel>> channelsCache;
    private final LoadingCache<Object, Set<UnresolvedClosedChannel>> closedChannelsCache;

    public ChannelService(GrpcChannels grpcChannels) {
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getUnresolvedClosedChannels);
    }

    public Set<LocalOpenChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Set<UnresolvedClosedChannel> getClosedChannels() {
        return closedChannelsCache.getUnchecked("");
    }

    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }
}
