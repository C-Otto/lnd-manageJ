package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.warnings.ChannelNumUpdatesWarning;
import de.cotto.lndmanagej.model.warnings.ChannelWarning;
import de.cotto.lndmanagej.service.warnings.ChannelWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.MAX_NUM_UPDATES;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.MAX_NUM_UPDATES_IGNORE_CHANNEL;

@Component
public class ChannelNumUpdatesWarningsProvider implements ChannelWarningsProvider {
    private static final int DEFAULT_MAX_NUM_UPDATES = 100_000;

    private final ChannelService channelService;
    private final ConfigurationService configurationService;

    public ChannelNumUpdatesWarningsProvider(ChannelService channelService, ConfigurationService configurationService) {
        this.channelService = channelService;
        this.configurationService = configurationService;
    }

    @Override
    public Stream<ChannelWarning> getChannelWarnings(ChannelId channelId) {
        if (ignoreWarning(channelId)) {
            return Stream.empty();
        }
        return Stream.of(getNumUpdatesWarning(channelId)).flatMap(Optional::stream);
    }

    private Optional<ChannelWarning> getNumUpdatesWarning(ChannelId channelId) {
        Integer maxNumUpdates = configurationService.getIntegerValue(MAX_NUM_UPDATES)
                .orElse(DEFAULT_MAX_NUM_UPDATES);
        long numUpdates = channelService.getOpenChannel(channelId).map(LocalOpenChannel::getNumUpdates).orElse(0L);
        if (numUpdates <= maxNumUpdates) {
            return Optional.empty();
        }
        return Optional.of(new ChannelNumUpdatesWarning(numUpdates));
    }

    private boolean ignoreWarning(ChannelId channelId) {
        return configurationService.getChannelIds(MAX_NUM_UPDATES_IGNORE_CHANNEL).contains(channelId);
    }
}
