package de.cotto.lndmanagej.service;

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
    private static final int LOWER_THRESHOLD = 10;
    private static final int UPPER_THRESHOLD = 90;
    private static final int DAYS = 14;

    private final ChannelService channelService;
    private final BalanceService balanceService;

    public ChannelBalanceFluctuationWarningsProvider(ChannelService channelService, BalanceService balanceService) {
        this.channelService = channelService;
        this.balanceService = balanceService;
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
        if (minPercentage < LOWER_THRESHOLD && maxPercentage > UPPER_THRESHOLD) {
            return Optional.of(new ChannelBalanceFluctuationWarning(minPercentage, maxPercentage, DAYS));
        }
        return Optional.empty();
    }
}
