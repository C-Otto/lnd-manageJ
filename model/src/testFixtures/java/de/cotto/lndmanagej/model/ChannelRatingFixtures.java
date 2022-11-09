package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;

public final class ChannelRatingFixtures {
    public static final ChannelRating RATING = ratingWithValue(123);

    private ChannelRatingFixtures() {
        // do not instantiate me
    }

    public static ChannelRating ratingWithValue(long value) {
        return ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(value, "something");
    }
}
