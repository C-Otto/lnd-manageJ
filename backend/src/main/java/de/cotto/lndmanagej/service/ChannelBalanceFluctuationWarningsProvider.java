package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.configuration.WarningsConfigurationSettings;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.warnings.ChannelBalanceFluctuationWarning;
import de.cotto.lndmanagej.model.warnings.ChannelWarning;
import de.cotto.lndmanagej.service.warnings.ChannelWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ChannelBalanceFluctuationWarningsProvider implements ChannelWarningsProvider {
    private static final int DAYS = 14;
    private static final int DEFAULT_LOWER_THRESHOLD = 10;
    private static final int DEFAULT_UPPER_THRESHOLD = 90;

    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final ConfigurationService configurationService;

    public ChannelBalanceFluctuationWarningsProvider(
            ChannelService channelService,
            BalanceService balanceService,
            ConfigurationService configurationService
    ) {
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.configurationService = configurationService;
    }

    @Override
    public Stream<ChannelWarning> getChannelWarnings(ChannelId channelId) {
        return Stream.of(getBalanceFluctuatingWarning(channelId)).flatMap(Optional::stream);
    }

    private Optional<ChannelWarning> getBalanceFluctuatingWarning(ChannelId channelId) {
        Coins capacity = channelService.getLocalChannel(channelId).map(LocalChannel::getCapacity).orElse(null);
        if (capacity == null) {
            return Optional.empty();
        }
        Coins min = balanceService.getLocalBalanceMinimum(channelId, DAYS).orElse(null);
        Coins max = balanceService.getLocalBalanceMaximum(channelId, DAYS).orElse(null);
        if (min == null || max == null) {
            return Optional.empty();
        }
        int minPercentage = (int) (min.milliSatoshis() * 100.0 / capacity.milliSatoshis());
        int maxPercentage = (int) (max.milliSatoshis() * 100.0 / capacity.milliSatoshis());
        if (minPercentage < getLowerThreshold() && maxPercentage > getUpperThreshold()) {
            return Optional.of(new ChannelBalanceFluctuationWarning(minPercentage, maxPercentage, DAYS));
        }
        return Optional.empty();
    }

    private int getLowerThreshold() {
        return configurationService.getIntegerValue(WarningsConfigurationSettings.CHANNEL_FLUCTUATION_LOWER_THRESHOLD)
                .orElse(DEFAULT_LOWER_THRESHOLD);
    }

    private int getUpperThreshold() {
        return configurationService.getIntegerValue(WarningsConfigurationSettings.CHANNEL_FLUCTUATION_UPPER_THRESHOLD)
                .orElse(DEFAULT_UPPER_THRESHOLD);
    }
}
