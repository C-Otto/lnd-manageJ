package de.cotto.lndmanagej.model;

import static java.util.Objects.requireNonNull;

public class BreachForceClosedChannelBuilder extends ClosedChannelBuilder<BreachForceClosedChannel> {
    public BreachForceClosedChannelBuilder() {
        super();
    }

    @Override
    public BreachForceClosedChannel build() {
        return new BreachForceClosedChannel(
                getChannelCoreInformation(),
                requireNonNull(ownPubkey),
                requireNonNull(remotePubkey),
                requireNonNull(closeTransactionHash),
                requireNonNull(openInitiator),
                closeHeight,
                resolutions
        );
    }
}