package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChannelIdResolverImpl implements ChannelIdResolver {
    public ChannelIdResolverImpl() {
        // default constructor
    }

    @Override
    public Optional<ChannelId> resolveFromChannelPoint(ChannelPoint channelPoint) {
        return Optional.empty();
    }
}
