package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelRatingFixtures.ratingWithValue;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ClassCanBeStatic")
class PeerRatingTest {
    private static final String RATING = "%s rating";

    private final PeerRating peerRating = PeerRating.forPeer(PUBKEY);
    private final PeerRating oneChannelRating = peerRating.withChannelRating(ratingWithValue(123));

    @Nested
    class NoChannelRatings {
        @Test
        void value() {
            assertThat(peerRating.getValue()).isZero();
        }

        @Test
        void descriptions() {
            Map<String, Number> descriptions = peerRating.getDescriptions();
            assertThat(descriptions).isEmpty();
        }
    }

    @Nested
    class OneChannelRating {
        @Test
        void value() {
            assertThat(oneChannelRating.getValue()).isEqualTo(123);
        }

        @Test
        void descriptions() {
            assertThat(oneChannelRating.getDescriptions())
                    .containsEntry(RATING.formatted(PUBKEY), 123L)
                    .containsEntry(RATING.formatted(CHANNEL_ID), 123L);
        }
    }

    @Nested
    class TwoChannelRatings {
        private final PeerRating twoChannelsRating = oneChannelRating.withChannelRating(
                ChannelRating.forChannel(CHANNEL_ID_2).addValueWithDescription(900, "foo!")
        );

        @Test
        void value() {
            assertThat(twoChannelsRating.getValue()).isEqualTo(1023L);
        }

        @Test
        void descriptions() {
            assertThat(twoChannelsRating.getDescriptions())
                    .containsEntry(RATING.formatted(PUBKEY), 1023L)
                    .containsEntry(RATING.formatted(CHANNEL_ID), 123L)
                    .containsEntry(RATING.formatted(CHANNEL_ID_2), 900L);
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PeerRating.class).verify();
    }
}
