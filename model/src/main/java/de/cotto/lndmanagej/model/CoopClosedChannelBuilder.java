package de.cotto.lndmanagej.model;

import static java.util.Objects.requireNonNull;

public class CoopClosedChannelBuilder extends ClosedChannelBuilder<CoopClosedChannel> {
    public CoopClosedChannelBuilder() {
        super();
    }

    @Override
    public CoopClosedChannel build() {
        return new CoopClosedChannel(
                requireNonNull(channelId),
                requireNonNull(channelPoint),
                requireNonNull(capacity),
                requireNonNull(ownPubkey),
                requireNonNull(remotePubkey),
                requireNonNull(closeTransactionHash),
                requireNonNull(openInitiator),
                requireNonNull(closeInitiator)
        );
    }
}