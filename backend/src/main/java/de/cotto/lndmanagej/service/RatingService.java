package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;

@Component
public class RatingService {
    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final double MINUTES_PER_DAY = 24 * 60;
    private static final int DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS = 30;
    private static final int DEFAULT_DAYS_FOR_ANALYSIS = 30;
    private static final Duration EXPIRY = Duration.ofHours(1);
    private static final Duration REFRESH = Duration.ofMillis(30);

    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final PolicyService policyService;
    private final ConfigurationService configurationService;
    private final BalanceService balanceService;
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
            OwnNodeService ownNodeService,
            FeeService feeService,
            RebalanceService rebalanceService,
            PolicyService policyService,
            ConfigurationService configurationService,
            BalanceService balanceService
    ) {
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
        this.policyService = policyService;
        this.configurationService = configurationService;
        this.balanceService = balanceService;
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
        FeeReport feeReport =
                feeService.getFeeReportForChannel(channelId, durationForAnalysis);
        RebalanceReport rebalanceReport =
                rebalanceService.getReportForChannel(channelId, durationForAnalysis);
        long feeRate = policyService.getMinimumFeeRateTo(localChannel.getRemotePubkey()).orElse(0L);
        long localAvailableMilliSat = getLocalAvailableMilliSat(localChannel);
        double millionSat = 1.0 * localAvailableMilliSat / 1_000 / 1_000_000;
        long averageSat = balanceService.getLocalBalanceAverage(channelId, (int) durationForAnalysis.toDays())
                .orElse(Coins.NONE).satoshis();

        long rating = feeReport.earned().milliSatoshis();
        rating += feeReport.sourced().milliSatoshis();
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

    private Set<ChannelId> getEligibleChannelsWithoutCache(Pubkey peer) {
        Set<LocalOpenChannel> openChannels = channelService.getOpenChannelsWith(peer);
        Set<ChannelId> result = new LinkedHashSet<>(openChannels.stream().map(Channel::getId).toList());
        if (openChannels.isEmpty()) {
            return result;
        }
        int minHeight = getEarliestOpenHeight(openChannels).orElseThrow();
        while (true) {
            List<ClosedChannel> recentlyClosed = getClosedChannelsClosedAtOrAfter(peer, minHeight);
            result.addAll(recentlyClosed.stream().map(Channel::getId).toList());
            int newMinHeight = getEarliestOpenHeight(recentlyClosed).orElse(minHeight);
            if (newMinHeight >= minHeight) {
                break;
            }
            minHeight = newMinHeight;
        }
        int ageInDays = getAgeInDays(minHeight);
        if (ageInDays < getDefaultMinAgeDaysForAnalysis()) {
            return Set.of();
        }
        return result;
    }

    private long getLocalAvailableMilliSat(LocalChannel localChannel) {
        if (localChannel instanceof LocalOpenChannel openChannel) {
            return openChannel.getBalanceInformation().localAvailable().milliSatoshis();
        }
        return 0;
    }

    private int getDefaultMinAgeDaysForAnalysis() {
        return configurationService.getIntegerValue(MIN_AGE_DAYS_FOR_ANALYSIS)
                .orElse(DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS);
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

    private int getAgeInDays(int openHeight) {
        int channelAgeInBlocks = ownNodeService.getBlockHeight() - openHeight;
        return (int) Math.floor(channelAgeInBlocks * 1.0 * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
    }

    private OptionalInt getEarliestOpenHeight(Collection<? extends Channel> channels) {
        return channels.stream().mapToInt(c -> c.getId().getBlockHeight()).min();
    }

    private List<ClosedChannel> getClosedChannelsClosedAtOrAfter(Pubkey peer, int minHeight) {
        return channelService.getClosedChannelsWith(peer).stream()
                .filter(c -> c.getCloseHeight() >= minHeight)
                .toList();
    }
}
