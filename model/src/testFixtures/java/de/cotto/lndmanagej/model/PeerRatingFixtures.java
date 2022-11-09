package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public final class PeerRatingFixtures {
    public static final PeerRating PEER_RATING = ratingWithValue(123);

    private PeerRatingFixtures() {
        // do not instantiate me
    }

    public static PeerRating ratingWithValue(long value) {
        return PeerRating.forPeer(PUBKEY).withChannelRating(ChannelRatingFixtures.ratingWithValue(value));
    }
}
