package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChannelService {
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final LoadingCache<Object, Set<LocalOpenChannel>> channelsCache;
    private final LoadingCache<Object, Set<ClosedChannel>> closedChannelsCache;
    private final GrpcChannels grpcChannels;

    public ChannelService(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(this.grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(this::getClosedChannelsWithoutCache);
    }

    public Set<LocalOpenChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Set<ClosedChannel> getClosedChannels() {
        return closedChannelsCache.getUnchecked("");
    }

    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    public Set<LocalChannel> getAllChannelsWith(Pubkey pubkey) {
        Stream<LocalOpenChannel> openChannels = getOpenChannelsWith(pubkey).stream();
        Stream<ClosedChannel> closedChannels = getClosedChannels().stream()
                .filter(c -> c.getRemotePubkey().equals(pubkey));
        return Stream.of(openChannels, closedChannels).flatMap(s -> s)
                .collect(Collectors.toSet());
    }

    private Set<ClosedChannel> getClosedChannelsWithoutCache() {
        return grpcChannels.getClosedChannels();
    }
}
