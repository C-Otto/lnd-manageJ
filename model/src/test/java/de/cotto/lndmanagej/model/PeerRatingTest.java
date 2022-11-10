package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelRatingFixtures.ratingWithValue;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ClassCanBeStatic")
class PeerRatingTest {
    private static final String RATING = "%s rating";
    private static final String RAW_RATING = "%s raw rating";

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
                    .containsEntry(RAW_RATING.formatted(CHANNEL_ID), 123L);
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
                    .containsEntry(RAW_RATING.formatted(CHANNEL_ID), 123L)
                    .containsEntry(RAW_RATING.formatted(CHANNEL_ID_2), 900L);
        }
    }

    @Nested
    class LocalBalance {
        @Test
        void one_channel() {
            PeerRating twoChannelsRating = peerRating
                    .withChannelRating(create(CHANNEL_ID, 1, 30, 100));
            assertThat(twoChannelsRating.getValue()).isEqualTo(100);
        }

        @Test
        void two_identical_channels() {
            PeerRating twoChannelsRating = peerRating
                    .withChannelRating(create(CHANNEL_ID, 1, 30, 100))
                    .withChannelRating(create(CHANNEL_ID_2, 1, 30, 100));
            assertThat(twoChannelsRating.getValue()).isEqualTo(100);
        }

        @Test
        void one_idle_channel_without_liquidity() {
            PeerRating twoChannelsRating = peerRating
                    .withChannelRating(create(CHANNEL_ID, 1, 30, 100))
                    .withChannelRating(create(CHANNEL_ID_2, 0, 30, 0));
            assertThat(twoChannelsRating.getValue()).isEqualTo(100);
        }

        @Test
        void one_idle_channel_with_liquidity() {
            PeerRating twoChannelsRating = peerRating
                    .withChannelRating(create(CHANNEL_ID, 1, 30, 100))
                    .withChannelRating(create(CHANNEL_ID_2, 1, 30, 0));
            assertThat(twoChannelsRating.getValue()).isEqualTo(50);
        }

        @Test
        void one_good_channel_one_idle_channel_with_liquidity_for_a_shorter_time() {
            int balance1 = 2;
            int balance2 = 4;
            int days1 = 30;
            int days2 = 15;
            PeerRating twoChannelsRating = peerRating
                    .withChannelRating(create(CHANNEL_ID, balance1, days1, 100))
                    .withChannelRating(create(CHANNEL_ID_2, balance2, days2, 0));
            long expected = (long) (100.0 / (days1 / 30.0 * balance1 + days2 / 30.0 * balance2));
            assertThat(twoChannelsRating.getValue()).isEqualTo(expected);
        }

        private ChannelRating create(ChannelId channelId, long averageLocalBalance, int days, int unscaledRating) {
            CoinsAndDuration coinsAndDuration = new CoinsAndDuration(
                    Coins.ofSatoshis(averageLocalBalance * 1_000_000),
                    Duration.ofDays(days)
            );
            return ChannelRating.forChannel(channelId)
                    .addValueWithDescription(unscaledRating, "earned")
                    .forAverageLocalBalance(coinsAndDuration);
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PeerRating.class).verify();
    }
}
