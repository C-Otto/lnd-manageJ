package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelRating;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.PeerRating;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;

@Component
public class RatingService {
    private static final int DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS = 30;
    private static final Duration EXPIRY = Duration.ofHours(1);
    private static final Duration REFRESH = Duration.ofMillis(30);

    private final ChannelService channelService;
    private final ConfigurationService configurationService;
    private final OverlappingChannelsService overlappingChannelsService;
    private final RatingForChannelService ratingForChannelService;
    private final LoadingCache<Pubkey, Optional<PeerRating>> peerCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getRatingForPeerWithoutCache);
    private final LoadingCache<ChannelId, Optional<ChannelRating>> channelCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getRatingForChannelWithoutCache);
    private final LoadingCache<Pubkey, Set<ChannelId>> getEligibleChannelsCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getEligibleChannelsWithoutCache);

    public RatingService(
            ChannelService channelService,
            ConfigurationService configurationService,
            OverlappingChannelsService overlappingChannelsService,
            RatingForChannelService ratingForChannelService
    ) {
        this.channelService = channelService;
        this.configurationService = configurationService;
        this.overlappingChannelsService = overlappingChannelsService;
        this.ratingForChannelService = ratingForChannelService;
    }

    public Optional<PeerRating> getRatingForPeer(Pubkey peer) {
        return peerCache.get(peer);
    }

    public Optional<ChannelRating> getRatingForChannel(ChannelId channelId) {
        return channelCache.get(channelId);
    }

    private Optional<ChannelRating> getRatingForChannel(ChannelId channelId, Set<ChannelId> eligibleChannels) {
        if (!eligibleChannels.contains(channelId)) {
            return Optional.empty();
        }
        return ratingForChannelService.getRating(channelId);
    }

    private Set<ChannelId> getEligibleChannels(Pubkey peer) {
        return getEligibleChannelsCache.get(peer);
    }

    private Set<ChannelId> getEligibleChannelsWithoutCache(Pubkey pubkey) {
        Set<ChannelId> candidates = overlappingChannelsService.getTransitiveOpenChannels(pubkey);
        if (candidates.isEmpty()) {
            return Set.of();
        }
        Duration ageOfOldestCandidate = overlappingChannelsService.getAgeOfEarliestOpenHeight(candidates);
        if (ageOfOldestCandidate.compareTo(getMinAgeDaysForAnalysis()) < 0) {
            return Set.of();
        }
        return candidates;
    }

    private Duration getMinAgeDaysForAnalysis() {
        int days = configurationService.getIntegerValue(MIN_AGE_DAYS_FOR_ANALYSIS)
                .orElse(DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS);
        return Duration.ofDays(days);
    }

    private Optional<ChannelRating> getRatingForChannelWithoutCache(ChannelId channelId) {
        return channelService.getOpenChannel(channelId).map(LocalChannel::getRemotePubkey)
                .map(this::getEligibleChannels)
                .flatMap(eligibleChannels -> getRatingForChannel(channelId, eligibleChannels));
    }

    private Optional<PeerRating> getRatingForPeerWithoutCache(Pubkey peer) {
        Set<ChannelId> eligibleChannels = getEligibleChannels(peer);
        List<ChannelRating> channelRatings = eligibleChannels.stream()
                .map(channelId -> getRatingForChannel(channelId, eligibleChannels))
                .flatMap(Optional::stream)
                .toList();
        if (channelRatings.isEmpty()) {
            return Optional.empty();
        }
        PeerRating peerRating = PeerRating.forPeer(peer);
        for (ChannelRating channelRating : channelRatings) {
            peerRating = peerRating.withChannelRating(channelRating);
        }
        return Optional.of(peerRating);
    }
}
