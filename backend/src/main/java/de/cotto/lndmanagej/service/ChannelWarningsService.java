package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.warnings.ChannelWarning;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.service.warnings.ChannelWarningsProvider;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class ChannelWarningsService {
    private final Collection<ChannelWarningsProvider> providers;
    private final ChannelService channelService;

    public ChannelWarningsService(
            Collection<ChannelWarningsProvider> providers,
            ChannelService channelService
    ) {
        this.providers = providers;
        this.channelService = channelService;
    }

    public ChannelWarnings getChannelWarnings(ChannelId channelId) {
        Set<ChannelWarning> warnings = providers.parallelStream()
                .flatMap(provider -> provider.getChannelWarnings(channelId))
                .collect(toSet());
        return new ChannelWarnings(warnings);
    }

    public Map<LocalOpenChannel, ChannelWarnings> getChannelWarnings() {
        return channelService.getOpenChannels().parallelStream()
                .map(channel -> new AbstractMap.SimpleEntry<>(channel, getChannelWarnings(channel.getId())))
                .filter(this::hasWarnings)
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private boolean hasWarnings(Entry<?, ChannelWarnings> entry) {
        return !entry.getValue().warnings().isEmpty();
    }
}
