package de.cotto.lndmanagej.model;

import static java.util.Objects.requireNonNull;

public class BreachForceClosedChannelBuilder extends ClosedChannelBuilder<BreachForceClosedChannel> {
    public BreachForceClosedChannelBuilder() {
        super();
    }

    @Override
    public BreachForceClosedChannel build() {
        return new BreachForceClosedChannel(
                requireNonNull(channelId),
                requireNonNull(channelPoint),
                requireNonNull(capacity),
                requireNonNull(ownPubkey),
                requireNonNull(remotePubkey),
                requireNonNull(closeTransactionHash),
                requireNonNull(openInitiator)
        );
    }
}