package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@SuppressWarnings("ClassCanBeStatic")
class ChannelRatingTest {
    private static final String RAW_RATING = "%s raw rating";
    private static final String SCALED_RATING = "%s rating scaled by local balance";
    private static final String AVERAGE_LOCAL_BALANCE = "%s average local balance (million sat) for %s days";

    @Test
    void initial_value_is_zero() {
        assertThat(ChannelRating.forChannel(CHANNEL_ID).getValue()).isZero();
    }

    @Nested
    class ForChannel {
        private final ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID);

        @Test
        void has_value_zero() {
            assertThat(rating.getValue()).isZero();
        }

        @Test
        void has_description_with_channel_id() {
            assertThat(rating.getDescriptions()).containsEntry(RAW_RATING.formatted(CHANNEL_ID), 0L);
        }
    }

    @Nested
    class Combine {
        @Test
        void empty_and_empty() {
            assertThat(ChannelRating.forChannel(CHANNEL_ID).combine(ChannelRating.forChannel(CHANNEL_ID)))
                    .isEqualTo(ChannelRating.forChannel(CHANNEL_ID));
        }

        @Test
        void empty_and_something() {
            ChannelRating something = ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(1, "foo");
            assertThat(ChannelRating.forChannel(CHANNEL_ID).combine(something)).isEqualTo(something);
        }

        @Test
        void something_and_empty() {
            ChannelRating something = ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(1, "foo");
            assertThat(something.combine(ChannelRating.forChannel(CHANNEL_ID))).isEqualTo(something);
        }

        @Test
        void something_and_something() {
            ChannelRating something = ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(1, "a");
            assertThat(something.combine(something).getValue()).isEqualTo(2);
        }

        @Test
        void different_channels() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> ChannelRating.forChannel(CHANNEL_ID).combine(ChannelRating.forChannel(CHANNEL_ID_2))
            ).withMessage("Ratings created for different channels (%s and %s)".formatted(CHANNEL_ID, CHANNEL_ID_2));
        }

        @Test
        void updates_descriptions() {
            int days = 42;
            double millionSat = 0.5;
            ChannelRating withDescriptions1 = ChannelRating.forChannel(CHANNEL_ID)
                    .addValueWithDescription(456, "a")
                    .addValueWithDescription(789, "c")
                    .forAverageLocalBalance(new CoinsAndDuration(
                            Coins.ofSatoshis((long) (millionSat * 1_000_000)),
                            Duration.ofDays(days)
                    ));
            ChannelRating withDescriptions2 = ChannelRating.forChannel(CHANNEL_ID)
                    .addValueWithDescription(111, "e")
                    .addValueWithDescription(123, "g");
            assertThat(withDescriptions1.combine(withDescriptions2).getDescriptions())
                    .isEqualTo(Map.of(
                            CHANNEL_ID + " a", 456L,
                            CHANNEL_ID + " c", 789L,
                            CHANNEL_ID + " e", 111L,
                            CHANNEL_ID + " g", 123L,
                            AVERAGE_LOCAL_BALANCE.formatted(CHANNEL_ID, days), millionSat,
                            RAW_RATING.formatted(CHANNEL_ID), 456L + 789 + 111 + 123,
                            SCALED_RATING.formatted(CHANNEL_ID), (long) ((456L + 789 + 111 + 123) / millionSat)
                    ));
        }

        @Test
        void with_average_local_balance_combined_with_instance_without() {
            CoinsAndDuration expected = new CoinsAndDuration(Coins.ofSatoshis(1_000_000), Duration.ofDays(1));
            ChannelRating with = ChannelRating.forChannel(CHANNEL_ID).forAverageLocalBalance(expected);
            ChannelRating without = ChannelRating.forChannel(CHANNEL_ID);
            assertThat(with.combine(without).getAverageLocalBalance()).isEqualTo(expected);
        }

        @Test
        void without_average_local_balance_combined_with_instance_with() {
            CoinsAndDuration expected = new CoinsAndDuration(Coins.ofSatoshis(1_000_000), Duration.ofDays(1));
            ChannelRating with = ChannelRating.forChannel(CHANNEL_ID).forAverageLocalBalance(expected);
            ChannelRating without = ChannelRating.forChannel(CHANNEL_ID);
            assertThat(without.combine(with).getAverageLocalBalance()).isEqualTo(expected);
        }
    }

    @Nested
    class AddValueWithDescription {
        @Test
        void adds_value() {
            ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(111, "a");
            assertThat(rating.addValueWithDescription(123, "c").getValue()).isEqualTo(234L);
        }

        @Test
        void updates_rating_description() {
            ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID);
            assertThat(rating.addValueWithDescription(123, "foo").getDescriptions())
                    .containsEntry(RAW_RATING.formatted(CHANNEL_ID), 123L);
        }

        @Test
        void extends_descriptions() {
            ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID).addValueWithDescription(111, "a");
            assertThat(rating.addValueWithDescription(123, "c").getDescriptions())
                    .isEqualTo(Map.of(
                            CHANNEL_ID + " a", 111L,
                            CHANNEL_ID + " c", 123L,
                            RAW_RATING.formatted(CHANNEL_ID), 234L
                    ));
        }
    }

    @Nested
    class ForDays {
        private final ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID)
                .addValueWithDescription(100, "a");

        @Test
        void scales_value() {
            assertThat(rating.forDays(3).getValue())
                    .isEqualTo(33L);
        }

        @Test
        void extends_description() {
            Map<String, Number> expectedDescriptions = Map.of(
                    CHANNEL_ID + " a", 100L,
                    RAW_RATING.formatted(CHANNEL_ID), 33L,
                    CHANNEL_ID + " scaled by days", 1.0 / 3
            );
            assertThat(rating.forDays(3).getDescriptions())
                    .isEqualTo(expectedDescriptions);
        }
    }

    @Nested
    class ForAverageLocalBalance {
        private final ChannelRating rating = ChannelRating.forChannel(CHANNEL_ID)
                .addValueWithDescription(100, "a");

        @Test
        void scales_value() {
            CoinsAndDuration averageLocalBalance = new CoinsAndDuration(Coins.ofSatoshis(2_000_000), Duration.ZERO);
            assertThat(rating.forAverageLocalBalance(averageLocalBalance).getValue())
                    .isEqualTo(50);
        }

        @Test
        void extends_description() {
            int days = 123;
            CoinsAndDuration averageLocalBalance = new CoinsAndDuration(
                    Coins.ofSatoshis(500_000),
                    Duration.ofDays(days)
            );
            Map<String, Number> expectedDescriptions = Map.of(
                    CHANNEL_ID + " a", 100L,
                    RAW_RATING.formatted(CHANNEL_ID), 100L,
                    SCALED_RATING.formatted(CHANNEL_ID), 200L,
                    AVERAGE_LOCAL_BALANCE.formatted(CHANNEL_ID, days), 0.5
            );
            assertThat(rating.forAverageLocalBalance(averageLocalBalance).getDescriptions())
                    .isEqualTo(expectedDescriptions);
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ChannelRating.class).verify();
    }
}
