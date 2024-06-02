package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.feerates.FeeRates;
import de.cotto.lndmanagej.feerates.FeeRatesDao;
import de.cotto.lndmanagej.model.FeeRateInformation;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class FeeRateUpdater {
    private final ChannelService channelService;
    private final PolicyService policyService;
    private final FeeRatesDao feeRatesDao;

    public FeeRateUpdater(ChannelService channelService, PolicyService policyService, FeeRatesDao feeRatesDao) {
        this.channelService = channelService;
        this.policyService = policyService;
        this.feeRatesDao = feeRatesDao;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void storeFeeRates() {
        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
        channelService.getOpenChannels().forEach(channel -> storeFeeRates(channel, timestamp));
    }

    private void storeFeeRates(LocalOpenChannel channel, LocalDateTime timestamp) {
        PoliciesForLocalChannel policies = policyService.getPolicies(channel);
        FeeRateInformation feeRateInformation = FeeRateInformation.fromPolicies(policies);

        Optional<FeeRates> persistedFeeRates = feeRatesDao.getMostRecentFeeRates(channel.getId());
        if (persistedFeeRates.isPresent() && feeRateInformation.equals(persistedFeeRates.get().feeRates())) {
            return;
        }
        feeRatesDao.saveFeeRates(new FeeRates(timestamp, channel.getId(), feeRateInformation));
    }
}
