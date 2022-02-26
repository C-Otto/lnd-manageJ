package de.cotto.lndmanagej.service.warnings;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.warnings.ChannelWarning;

import java.util.stream.Stream;

public interface ChannelWarningsProvider {
    Stream<ChannelWarning> getChannelWarnings(ChannelId channelId);
}
