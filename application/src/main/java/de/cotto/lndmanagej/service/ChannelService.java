package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcClosedChannels;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.WaitingCloseChannel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChannelService {
    private static final int CACHE_EXPIRY_MINUTES = 1;

    private final GrpcChannels grpcChannels;
    private final LoadingCache<Object, Set<LocalOpenChannel>> channelsCache;
    private final LoadingCache<Object, Set<ClosedChannel>> closedChannelsCache;
    private final LoadingCache<Object, Set<ForceClosingChannel>> forceClosingChannelsCache;
    private final LoadingCache<Object, Set<WaitingCloseChannel>> waitingCloseChannelsCache;

    public ChannelService(GrpcChannels grpcChannels, GrpcClosedChannels grpcClosedChannels) {
        this.grpcChannels = grpcChannels;
        channelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcClosedChannels::getClosedChannels);
        forceClosingChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getForceClosingChannels);
        waitingCloseChannelsCache = new CacheBuilder()
                .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
                .build(grpcChannels::getWaitingCloseChannels);
    }

    public boolean isClosed(ChannelId channelId) {
        return getClosedChannel(channelId).isPresent();
    }

    public Optional<LocalChannel> getLocalChannel(ChannelId channelId) {
        return getAllLocalChannels()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    public Set<LocalOpenChannel> getOpenChannels() {
        return channelsCache.getUnchecked("");
    }

    public Optional<LocalOpenChannel> getOpenChannel(ChannelId channelId) {
        return grpcChannels.getChannel(channelId);
    }

    public Set<ClosedChannel> getClosedChannels() {
        return closedChannelsCache.getUnchecked("");
    }

    public Optional<ClosedChannel> getClosedChannel(ChannelId channelId) {
        return getClosedChannels().stream()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    public Set<ForceClosingChannel> getForceClosingChannels() {
        return forceClosingChannelsCache.getUnchecked("");
    }

    public Optional<ForceClosingChannel> getForceClosingChannel(ChannelId channelId) {
        return getForceClosingChannels().stream()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    public Set<WaitingCloseChannel> getWaitingCloseChannels() {
        return waitingCloseChannelsCache.getUnchecked("");
    }

    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    public Set<LocalChannel> getAllChannelsWith(Pubkey peer) {
        return getAllLocalChannels()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    public Stream<LocalChannel> getAllLocalChannels() {
        Supplier<Set<LocalOpenChannel>> openChannels = this::getOpenChannels;
        Supplier<Set<ClosedChannel>> closedChannels = this::getClosedChannels;
        Supplier<Set<WaitingCloseChannel>> waitingCloseChannels = this::getWaitingCloseChannels;
        Supplier<Set<ForceClosingChannel>> forceClosingChannels = this::getForceClosingChannels;
        return Stream.of(
                openChannels,
                closedChannels,
                waitingCloseChannels,
                forceClosingChannels
        ).map(Supplier::get).flatMap(Collection::stream);
    }
}
