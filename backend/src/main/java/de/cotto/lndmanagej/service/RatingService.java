package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;

@Component
public class RatingService {
    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final double MINUTES_PER_DAY = 24 * 60;
    private static final int DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS = 30;
    private static final int DEFAULT_DAYS_FOR_ANALYSIS = 30;

    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final PolicyService policyService;
    private final ConfigurationService configurationService;
    private final BalanceService balanceService;

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
        return channelService.getOpenChannelsWith(peer).stream()
                .map(Channel::getId).map(this::getRatingForChannel)
                .flatMap(Optional::stream)
                .reduce(Rating.EMPTY, Rating::add);
    }

    public Optional<Rating> getRatingForChannel(ChannelId channelId) {
        int ageInDays = getAgeInDays(channelId);
        if (ageInDays < getDefaultMinAgeDaysForAnalysis()) {
            return Optional.of(Rating.EMPTY);
        }
        LocalOpenChannel localOpenChannel = channelService.getOpenChannel(channelId).orElse(null);
        if (localOpenChannel == null) {
            return Optional.empty();
        }
        Duration durationForAnalysis = getDurationForAnalysis();
        FeeReport feeReport =
                feeService.getFeeReportForChannel(channelId, durationForAnalysis);
        RebalanceReport rebalanceReport =
                rebalanceService.getReportForChannel(channelId, durationForAnalysis);
        long feeRate = policyService.getMinimumFeeRateTo(localOpenChannel.getRemotePubkey()).orElse(0L);
        long localAvailableMilliSat = localOpenChannel.getBalanceInformation().localAvailable().milliSatoshis();
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

    private int getDefaultMinAgeDaysForAnalysis() {
        return configurationService.getIntegerValue(MIN_AGE_DAYS_FOR_ANALYSIS)
                .orElse(DEFAULT_MIN_AGE_DAYS_FOR_ANALYSIS);
    }

    private Duration getDurationForAnalysis() {
        return Duration.ofDays(
                configurationService.getIntegerValue(DAYS_FOR_ANALYSIS).orElse(DEFAULT_DAYS_FOR_ANALYSIS)
        );
    }

    private int getAgeInDays(ChannelId channelId) {
        int channelAgeInBlocks = ownNodeService.getBlockHeight() - channelId.getBlockHeight();
        return (int) Math.ceil(channelAgeInBlocks * 1.0 * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
    }
}
