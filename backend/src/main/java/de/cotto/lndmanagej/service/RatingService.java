package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RatingService {
    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final double MINUTES_PER_DAY = 24 * 60;
    private static final int MIN_AGE_FOR_ANALYSIS_DAYS = 30;
    private static final Duration DURATION_FOR_ANALYSIS = Duration.ofDays(MIN_AGE_FOR_ANALYSIS_DAYS);

    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final PolicyService policyService;

    public RatingService(
            ChannelService channelService,
            OwnNodeService ownNodeService,
            FeeService feeService,
            RebalanceService rebalanceService,
            PolicyService policyService
    ) {
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
        this.policyService = policyService;
    }

    public Rating getRatingForPeer(Pubkey peer) {
        return channelService.getOpenChannelsWith(peer).stream()
                .map(Channel::getId).map(this::getRatingForChannel)
                .flatMap(Optional::stream)
                .reduce(Rating.EMPTY, Rating::add);
    }

    public Optional<Rating> getRatingForChannel(ChannelId channelId) {
        int ageInDays = getAgeInDays(channelId);
        if (ageInDays < MIN_AGE_FOR_ANALYSIS_DAYS) {
            return Optional.of(Rating.EMPTY);
        }
        LocalOpenChannel localOpenChannel = channelService.getOpenChannel(channelId).orElse(null);
        if (localOpenChannel == null) {
            return Optional.empty();
        }
        FeeReport feeReport =
                feeService.getFeeReportForChannel(channelId, DURATION_FOR_ANALYSIS);
        RebalanceReport rebalanceReport =
                rebalanceService.getReportForChannel(channelId, DURATION_FOR_ANALYSIS);
        long feeRate = policyService.getMinimumFeeRateTo(localOpenChannel.getRemotePubkey()).orElse(0L);
        long localAvailableMilliSat = localOpenChannel.getBalanceInformation().localAvailable().milliSatoshis();
        double millionSat = 1.0 * localAvailableMilliSat / 1_000 / 1_000_000;

        long rating = 1;
        rating += feeReport.earned().milliSatoshis();
        rating += feeReport.sourced().milliSatoshis();
        rating += rebalanceReport.supportAsSourceAmount().milliSatoshis() / 10_000;
        rating += rebalanceReport.supportAsTargetAmount().milliSatoshis() / 10_000;
        rating += (long) (1.0 * feeRate * millionSat / 10);
        long scaledRating = (long) (rating / Math.max(1, millionSat));
        return Optional.of(new Rating(scaledRating));
    }

    private int getAgeInDays(ChannelId channelId) {
        int channelAgeInBlocks = ownNodeService.getBlockHeight() - channelId.getBlockHeight();
        return (int) Math.ceil(channelAgeInBlocks * 1.0 * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
    }
}
