package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.UnresolvedClosedChannel;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelService {
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final LoadingCache<Object, Set<LocalOpenChannel>> channelsCache;
    private final LoadingCache<Object, Set<ClosedChannel>> closedChannelsCache;
    private final GrpcChannels grpcChannels;
    private final ChannelIdResolver channelIdResolver;

    public ChannelService(GrpcChannels grpcChannels, ChannelIdResolver channelIdResolver) {
        this.grpcChannels = grpcChannels;
        this.channelIdResolver = channelIdResolver;
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(this.grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(this::getClosedChannelsWithoutCache);
    }

    private Set<ClosedChannel> getClosedChannelsWithoutCache() {
        return grpcChannels.getUnresolvedClosedChannels().stream()
                .map(this::toClosedChannel)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private Optional<ClosedChannel> toClosedChannel(UnresolvedClosedChannel unresolvedClosedChannel) {
        if (unresolvedClosedChannel.getId().isUnresolved()) {
            return channelIdResolver.resolve(unresolvedClosedChannel.getChannelPoint())
                    .map(channelId -> ClosedChannel.create(unresolvedClosedChannel, channelId));
        }
        return Optional.of(ClosedChannel.create(unresolvedClosedChannel));
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
}
