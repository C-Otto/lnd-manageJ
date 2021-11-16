package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.WaitingCloseChannel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChannelService {
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final LoadingCache<Object, Set<LocalOpenChannel>> channelsCache;
    private final LoadingCache<Object, Set<ClosedChannel>> closedChannelsCache;
    private final LoadingCache<Object, Set<ForceClosingChannel>> forceClosingChannelsCache;
    private final LoadingCache<Object, Set<WaitingCloseChannel>> waitingCloseChannelsCache;

    public ChannelService(GrpcChannels grpcChannels) {
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getClosedChannels);
        forceClosingChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getForceClosingChannels);
        waitingCloseChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getWaitingCloseChannels);
    }

    public Set<LocalOpenChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Set<ClosedChannel> getClosedChannels() {
        return closedChannelsCache.getUnchecked("");
    }

    public Set<ForceClosingChannel> getForceClosingChannels() {
        return forceClosingChannelsCache.getUnchecked("");
    }

    public Set<WaitingCloseChannel> getWaitingCloseChannels() {
        return waitingCloseChannelsCache.getUnchecked("");
    }

    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    public Set<LocalChannel> getAllChannelsWith(Pubkey pubkey) {
        Set<LocalOpenChannel> openChannels = getOpenChannelsWith(pubkey);
        Set<WaitingCloseChannel> waitingCloseChannels = getWaitingCloseChannels();
        Set<ForceClosingChannel> forceClosingChannels = getForceClosingChannels();
        Set<ClosedChannel> closedChannels = getClosedChannels();
        return Stream.of(openChannels, closedChannels, waitingCloseChannels, forceClosingChannels)
                .flatMap(Collection::stream)
                .filter(c -> c.getRemotePubkey().equals(pubkey))
                .collect(Collectors.toSet());
    }
}
