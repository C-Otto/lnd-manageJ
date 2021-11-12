package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final LoadingCache<Object, Set<LocalChannel>> channelsCache;

    public ChannelService(GrpcChannels grpcChannels) {
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .withMaximumSize(MAXIMUM_SIZE)
                .build(grpcChannels::getChannels);
    }

    public Set<LocalChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Set<LocalChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

}
