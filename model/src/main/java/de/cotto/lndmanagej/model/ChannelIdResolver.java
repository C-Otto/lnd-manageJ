package de.cotto.lndmanagej.model;

import java.util.Optional;

public interface ChannelIdResolver {
    Optional<ChannelId> resolveFromChannelPoint(ChannelPoint channelPoint);
}
