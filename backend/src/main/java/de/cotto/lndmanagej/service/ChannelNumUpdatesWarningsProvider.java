package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.warnings.ChannelNumUpdatesWarning;
import de.cotto.lndmanagej.model.warnings.ChannelWarning;
import de.cotto.lndmanagej.service.warnings.ChannelWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ChannelNumUpdatesWarningsProvider implements ChannelWarningsProvider {
    private static final long MAX_NUM_UPDATES = 100_000L;
    private final ChannelService channelService;

    public ChannelNumUpdatesWarningsProvider(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    public Stream<ChannelWarning> getChannelWarnings(ChannelId channelId) {
        return Stream.of(getNumUpdatesWarning(channelId)).flatMap(Optional::stream);
    }

    private Optional<ChannelWarning> getNumUpdatesWarning(ChannelId channelId) {
        long numUpdates = channelService.getOpenChannel(channelId).map(LocalOpenChannel::getNumUpdates).orElse(0L);
        if (numUpdates <= MAX_NUM_UPDATES) {
            return Optional.empty();
        }
        return Optional.of(new ChannelNumUpdatesWarning(numUpdates));
    }
}
