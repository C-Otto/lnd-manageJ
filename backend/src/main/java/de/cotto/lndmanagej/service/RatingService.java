package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;

@Component
public class RatingService {
    private static final int DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS = 30;
    private static final int DEFAULT_DAYS_FOR_ANALYSIS = 30;
    private static final Duration EXPIRY = Duration.ofHours(1);
    private static final Duration REFRESH = Duration.ofMillis(30);

    private final ChannelService channelService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final PolicyService policyService;
    private final ConfigurationService configurationService;
    private final BalanceService balanceService;
    private final FlowService flowService;
    private final OverlappingChannelsService overlappingChannelsService;
    private final LoadingCache<Pubkey, Rating> peerCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getRatingForPeerWithoutCache);
    private final LoadingCache<ChannelId, Optional<Rating>> channelCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getRatingForChannelWithoutCache);
    private final LoadingCache<Pubkey, Set<ChannelId>> getEligibleChannelsCache = new CacheBuilder()
            .withExpiry(EXPIRY)
            .withRefresh(REFRESH)
            .build(this::getEligibleChannelsWithoutCache);

    public RatingService(
            ChannelService channelService,
            FeeService feeService,
            RebalanceService rebalanceService,
            PolicyService policyService,
            ConfigurationService configurationService,
            BalanceService balanceService,
            FlowService flowService,
            OverlappingChannelsService overlappingChannelsService
    ) {
        this.channelService = channelService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
        this.policyService = policyService;
        this.configurationService = configurationService;
        this.balanceService = balanceService;
        this.flowService = flowService;
        this.overlappingChannelsService = overlappingChannelsService;
    }

    public Rating getRatingForPeer(Pubkey peer) {
        return peerCache.get(peer);
    }

    public Optional<Rating> getRatingForChannel(ChannelId channelId) {
        return channelCache.get(channelId);
    }

    private Optional<Rating> getRatingForChannel(ChannelId channelId, Set<ChannelId> eligibleChannels) {
        if (!eligibleChannels.contains(channelId)) {
            return Optional.empty();
        }
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            return Optional.empty();
        }
        Duration durationForAnalysis = getDurationForAnalysis();
        Optional<Coins> averageLocalBalanceOptional =
                balanceService.getLocalBalanceAverage(channelId, (int) durationForAnalysis.toDays());
        if (averageLocalBalanceOptional.isEmpty()) {
            return Optional.empty();
        }
        FeeReport feeReport = feeService.getFeeReportForChannel(channelId, durationForAnalysis);
        RebalanceReport rebalanceReport = rebalanceService.getReportForChannel(channelId, durationForAnalysis);
        FlowReport flowReport = flowService.getFlowReportForChannel(channelId, durationForAnalysis);
        long feeRate = policyService.getMinimumFeeRateTo(localChannel.getRemotePubkey()).orElse(0L);
        long localAvailableMilliSat = getLocalAvailableMilliSat(localChannel);
        double millionSat = 1.0 * localAvailableMilliSat / 1_000 / 1_000_000;
        long averageSat = averageLocalBalanceOptional.get().satoshis();
        if (averageSat == 0) {
            averageSat = 1;
        }

        long rating = feeReport.earned().milliSatoshis();
        rating += feeReport.sourced().milliSatoshis();
        rating += flowReport.receivedViaPayments().milliSatoshis();
        rating += rebalanceReport.supportAsSourceAmount().milliSatoshis() / 10_000;
        rating += rebalanceReport.supportAsTargetAmount().milliSatoshis() / 10_000;
        rating += (long) (1.0 * feeRate * millionSat / 10);
        double scaledByLiquidity = 1.0 * rating * 1_000_000 / averageSat;
        double scaledByDays = scaledByLiquidity / durationForAnalysis.toDays();
        return Optional.of(new Rating((long) scaledByDays));
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

    private long getLocalAvailableMilliSat(LocalChannel localChannel) {
        if (localChannel instanceof LocalOpenChannel openChannel) {
            return openChannel.getBalanceInformation().localAvailable().milliSatoshis();
        }
        return 0;
    }

    private Duration getMinAgeDaysForAnalysis() {
        int days = configurationService.getIntegerValue(MIN_AGE_DAYS_FOR_ANALYSIS)
                .orElse(DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS);
        return Duration.ofDays(days);
    }

    private Optional<Rating> getRatingForChannelWithoutCache(ChannelId channelId) {
        return channelService.getOpenChannel(channelId).map(LocalChannel::getRemotePubkey)
                .map(this::getEligibleChannels)
                .flatMap(eligibleChannels -> getRatingForChannel(channelId, eligibleChannels));
    }

    private Rating getRatingForPeerWithoutCache(Pubkey peer) {
        Set<ChannelId> eligibleChannels = getEligibleChannels(peer);
        return eligibleChannels.stream()
                .map(channelId -> getRatingForChannel(channelId, eligibleChannels))
                .flatMap(Optional::stream)
                .reduce(Rating.EMPTY, Rating::add);
    }

    private Duration getDurationForAnalysis() {
        return Duration.ofDays(
                configurationService.getIntegerValue(DAYS_FOR_ANALYSIS).orElse(DEFAULT_DAYS_FOR_ANALYSIS)
        );
    }
}
