package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelRating;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;

@Component
public class RatingForChannelService {
    private static final int DEFAULT_DAYS_FOR_ANALYSIS = 30;

    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final FlowService flowService;
    private final PolicyService policyService;
    private final ConfigurationService configurationService;

    public RatingForChannelService(
            ChannelService channelService,
            BalanceService balanceService,
            FeeService feeService,
            RebalanceService rebalanceService,
            FlowService flowService,
            PolicyService policyService,
            ConfigurationService configurationService
    ) {
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
        this.flowService = flowService;
        this.policyService = policyService;
        this.configurationService = configurationService;
    }

    public Optional<ChannelRating> getRating(ChannelId channelId) {
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

        ChannelRating rating = ChannelRating.forChannel(channelId);
        rating = rating.addValueWithDescription(feeReport.earned().milliSatoshis(), "earned");
        rating = rating.addValueWithDescription(feeReport.sourced().milliSatoshis(), "sourced");
        rating = rating.addValueWithDescription(
                flowReport.receivedViaPayments().milliSatoshis(), "received via payments"
        );
        rating = rating.addValueWithDescription(
                rebalanceReport.supportAsSourceAmount().milliSatoshis() / 10_000, "support as source"
        );
        rating = rating.addValueWithDescription(
                rebalanceReport.supportAsTargetAmount().milliSatoshis() / 10_000, "support as target"
        );
        rating = rating.addValueWithDescription((long) (1.0 * feeRate * millionSat / 10), "future earnings");

        rating = rating.forAverageLocalBalance(averageLocalBalanceOptional.get());
        rating = rating.forDays(durationForAnalysis.toDays());

        return Optional.of(rating);
    }

    private long getLocalAvailableMilliSat(LocalChannel localChannel) {
        if (localChannel instanceof LocalOpenChannel openChannel) {
            return openChannel.getBalanceInformation().localAvailable().milliSatoshis();
        }
        return 0;
    }

    private Duration getDurationForAnalysis() {
        return Duration.ofDays(
                configurationService.getIntegerValue(DAYS_FOR_ANALYSIS).orElse(DEFAULT_DAYS_FOR_ANALYSIS)
        );
    }
}
