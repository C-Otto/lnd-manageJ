package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcClosedChannels;
import de.cotto.lndmanagej.model.Channel;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class ChannelService {
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(1);
    private static final Duration CACHE_REFRESH = Duration.ofSeconds(30);

    private final GrpcChannels grpcChannels;
    private final LoadingCache<Object, Set<LocalOpenChannel>> localOpenChannelsCache;
    private final LoadingCache<Object, Map<ChannelId, ClosedChannel>> closedChannelsCache;
    private final LoadingCache<Object, Set<ForceClosingChannel>> forceClosingChannelsCache;
    private final LoadingCache<Object, Set<WaitingCloseChannel>> waitingCloseChannelsCache;

    public ChannelService(
            GrpcChannels grpcChannels,
            GrpcClosedChannels grpcClosedChannels
    ) {
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
    public Collection<ClosedChannel> getClosedChannels() {
        return Objects.requireNonNull(closedChannelsCache.get("")).values();
    }

    @Timed
    public Set<ForceClosedChannel> getForceClosedChannels() {
        return getClosedChannels().stream()
                .filter(c -> c instanceof ForceClosedChannel)
                .map(c -> (ForceClosedChannel) c)
                .collect(toSet());
    }

    @Timed
    public Optional<ClosedChannel> getClosedChannel(ChannelId channelId) {
        Map<ChannelId, ClosedChannel> closedChannels = Objects.requireNonNull(closedChannelsCache.get(""));
        return Optional.ofNullable(closedChannels.get(channelId));
    }

    @Timed
    public Optional<ForceClosedChannel> getForceClosedChannel(ChannelId channelId) {
        return getClosedChannel(channelId)
                .filter(c -> c instanceof ForceClosedChannel)
                .map(c -> (ForceClosedChannel) c);
    }

    @Timed
    public Set<ForceClosingChannel> getForceClosingChannels() {
        return forceClosingChannelsCache.get("");
    }

    @Timed
    public Set<WaitingCloseChannel> getWaitingCloseChannels() {
        return waitingCloseChannelsCache.get("");
    }

    @Timed
    public Set<LocalOpenChannel> getOpenChannelsWith(Pubkey peer) {
        return getOpenChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(toSet());
    }

    @Timed
    public Set<ClosedChannel> getClosedChannelsWith(Pubkey peer) {
        return getClosedChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(toSet());
    }

    @Timed
    public Set<WaitingCloseChannel> getWaitingCloseChannelsWith(Pubkey peer) {
        return getWaitingCloseChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(toSet());
    }

    @Timed
    public Set<ForceClosingChannel> getForceClosingChannelsWith(Pubkey peer) {
        return getForceClosingChannels().stream()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(toSet());
    }

    @Timed
    public Set<LocalChannel> getAllChannelsWith(Pubkey peer) {
        return getAllLocalChannels()
                .filter(c -> peer.equals(c.getRemotePubkey()))
                .collect(toSet());
    }

    @Timed
    public Stream<LocalChannel> getAllLocalChannels() {
        Supplier<Set<LocalOpenChannel>> openChannels = this::getOpenChannels;
        Supplier<Collection<ClosedChannel>> closedChannels = this::getClosedChannels;
        Supplier<Set<WaitingCloseChannel>> waitingCloseChannels = this::getWaitingCloseChannels;
        Supplier<Set<ForceClosingChannel>> forceClosingChannels = this::getForceClosingChannels;
        return Stream.of(
                openChannels,
                closedChannels,
                waitingCloseChannels,
                forceClosingChannels
        ).parallel().map(Supplier::get).flatMap(Collection::stream);
    }

    public int getOpenHeight(Channel channel) {
        return channel.getId().getBlockHeight();
    }
}
