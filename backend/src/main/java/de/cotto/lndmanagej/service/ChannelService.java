package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcClosedChannels;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ForceClosedChannel;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.WaitingCloseChannel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChannelService {
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(1);
    private static final Duration CACHE_REFRESH = Duration.ofSeconds(30);

    private final GrpcChannels grpcChannels;
    private final LoadingCache<Object, Set<LocalOpenChannel>> localOpenChannelsCache;
    private final LoadingCache<Object, Set<ClosedChannel>> closedChannelsCache;
    private final LoadingCache<Object, Set<ForceClosingChannel>> forceClosingChannelsCache;
    private final LoadingCache<Object, Set<WaitingCloseChannel>> waitingCloseChannelsCache;

    public ChannelService(GrpcChannels grpcChannels, GrpcClosedChannels grpcClosedChannels) {
        this.grpcChannels = grpcChannels;
        localOpenChannelsCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(grpcChannels::getChannels);
        closedChannelsCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(grpcClosedChannels::getClosedChannels);
        forceClosingChannelsCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(grpcChannels::getForceClosingChannels);
        waitingCloseChannelsCache = new CacheBuilder()
                .withRefresh(CACHE_REFRESH)
                .withExpiry(CACHE_EXPIRY)
                .build(grpcChannels::getWaitingCloseChannels);
    }

    @Timed
    public boolean isClosed(ChannelId channelId) {
        return getClosedChannel(channelId).isPresent();
    }

    @Timed
    public boolean isForceClosed(ChannelId channelId) {
        return getClosedChannel(channelId).filter(ClosedChannel::isForceClosed).isPresent();
    }

    @Timed
    public Optional<LocalChannel> getLocalChannel(ChannelId channelId) {
        return getAllLocalChannels()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    @Timed
    public Set<LocalOpenChannel> getOpenChannels() {
        return localOpenChannelsCache.get("");
    }

    @Timed
    public Optional<LocalOpenChannel> getOpenChannel(ChannelId channelId) {
        return grpcChannels.getChannel(channelId);
    }

    @Timed
    public Set<ClosedChannel> getClosedChannels() {
        return closedChannelsCache.get("");
    }

    @Timed
    public Optional<ClosedChannel> getClosedChannel(ChannelId channelId) {
        return getClosedChannels().stream()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    @Timed
    public Optional<ForceClosedChannel> getForceClosedChannel(ChannelId channelId) {
        return getClosedChannels().stream()
                .filter(c -> channelId.equals(c.getId()))
                .filter(ClosedChannel::isForceClosed)
                .map(ClosedChannel::getAsForceClosedChannel)
                .findFirst();
    }

    @Timed
    public Set<ForceClosingChannel> getForceClosingChannels() {
        return forceClosingChannelsCache.get("");
    }

    @Timed
    public Optional<ForceClosingChannel> getForceClosingChannel(ChannelId channelId) {
        return getForceClosingChannels().stream()
                .filter(c -> channelId.equals(c.getId()))
                .findFirst();
    }

    @Timed
    public Set<WaitingCloseChannel> getWaitingCloseChannels() {
        return waitingCloseChannelsCache.get("");
    }

    @Timed
    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    @Timed
    public Set<ClosedChannel> getClosedChannelsWith(Pubkey peer) {
        return getClosedChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    @Timed
    public Set<WaitingCloseChannel> getWaitingCloseChannelsWith(Pubkey peer) {
        return getWaitingCloseChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    @Timed
    public Set<ForceClosingChannel> getForceClosingChannelsWith(Pubkey peer) {
        return getForceClosingChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    @Timed
    public Set<LocalChannel> getAllChannelsWith(Pubkey peer) {
        return getAllLocalChannels()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(Collectors.toSet());
    }

    @Timed
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
        ).parallel().map(Supplier::get).flatMap(Collection::stream);
    }
}
