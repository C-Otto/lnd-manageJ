package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.LiquidityBounds.NO_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
class LiquidityBoundsTest {

    private static final String CLASS_CAN_BE_STATIC = "ClassCanBeStatic";

    @Test
    void negative_lower_bound() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new LiquidityBounds(Coins.ofSatoshis(-1), null, Coins.NONE)
        ).withMessage("invalid lower bound: -1.000");
    }

    @Test
    void negative_upper_bound() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new LiquidityBounds(Coins.NONE, Coins.ofSatoshis(-1), Coins.NONE)
        ).withMessage("invalid upper bound: -1.000");
    }

    @Test
    void negative_in_flight() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new LiquidityBounds(Coins.NONE, null, Coins.ofSatoshis(-1))
        ).withMessage("invalid in flight amount: -1.000");
    }

    @Test
    void lower_bound_above_upper_bound() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new LiquidityBounds(Coins.ofSatoshis(100), Coins.ofSatoshis(99), Coins.NONE)
        ).withMessage("lower bound must not be above upper bound: 100.000 <! 99.000");
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class NoInformation {
        @Test
        void lower_bound() {
            assertThat(NO_INFORMATION.getLowerBound()).isEqualTo(Coins.NONE);
        }

        @Test
        void upper_bound() {
            assertThat(NO_INFORMATION.getUpperBound()).isEmpty();
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class WithAvailable {
        @Test
        void increases_lower_bound() {
            Coins amount = Coins.ofSatoshis(100);
            assertLowerBound(NO_INFORMATION.withAvailableCoins(amount), amount);
        }

        @Test
        void no_update_due_to_zero_amount_returns_empty_optional() {
            assertThat(NO_INFORMATION.withAvailableCoins(Coins.NONE)).isEmpty();
        }

        @Test
        void does_not_increase_lower_bound_if_not_above_old_lower_bound() {
            Coins amount = Coins.ofSatoshis(100);
            LiquidityBounds liquidityBounds = withLowerBound(amount);
            assertThat(liquidityBounds.withAvailableCoins(amount)).isEmpty();
        }

        @Test
        void increases_lower_bound_if_above_old_lower_bound() {
            Coins amountLow = Coins.ofSatoshis(100);
            Coins amountHigh = Coins.ofSatoshis(120);
            LiquidityBounds initial = withLowerBound(amountLow);
            assertLowerBound(initial.withAvailableCoins(amountHigh), amountHigh);
        }

        @Test
        void invalidates_upper_bound_if_amount_above_upper_bound_is_available() {
            Coins upperBound = Coins.ofSatoshis(100);
            Coins upperBoundPlusOneSat = upperBound.add(Coins.ofSatoshis(1));
            LiquidityBounds initial = withUpperBound(upperBound);
            LiquidityBounds updated = initial.withAvailableCoins(upperBoundPlusOneSat).orElseThrow();
            assertThat(updated.getUpperBound()).isEmpty();
        }

        @Test
        void keeps_upper_bound_identical_to_lower_bound() {
            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertUpperBound(initial.withAvailableCoins(upperBound), upperBound);
        }

        @Test
        void keeps_upper_bound_above_lower_bound() {
            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertUpperBound(initial.withAvailableCoins(Coins.ofSatoshis(10)), upperBound);
        }

        @Test
        void available_amount_below_in_flight() {
            LiquidityBounds initial = withInFlight(Coins.ofSatoshis(100));
            assertLowerBound(initial.withAvailableCoins(Coins.ofSatoshis(10)), Coins.NONE);
        }

        @Test
        void available_amount_above_in_flight() {
            LiquidityBounds initial = withInFlight(Coins.ofSatoshis(100));
            assertLowerBound(initial.withAvailableCoins(Coins.ofSatoshis(103)), Coins.ofSatoshis(3));
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class WithUnavailable {
        @Test
        void sets_upper_bound() {
            Coins amount = Coins.ofSatoshis(100);
            assertUpperBound(NO_INFORMATION.withUnavailableCoins(amount), oneSatLessThan(amount));
        }

        @Test
        void updates_upper_bound_with_amount_below_old_upper_bound() {
            Coins upperBound = Coins.ofSatoshis(120);
            LiquidityBounds initial = withUpperBound(upperBound);
            Coins amount = Coins.ofSatoshis(100);
            assertUpperBound(initial.withUnavailableCoins(amount), oneSatLessThan(amount));
        }

        @Test
        void ignores_update_with_amount_above_upper_bound() {
            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertThat(initial.withUnavailableCoins(Coins.ofSatoshis(120))).isEmpty();
        }

        @Test
        void keeps_lower_bound_if_below_upper_bound() {
            Coins lowerBound = Coins.ofSatoshis(300);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withUnavailableCoins(Coins.ofSatoshis(400)), lowerBound);
        }

        @Test
        void keeps_lower_bound_if_same_as_upper_bound() {
            Coins lowerBound = Coins.ofSatoshis(300);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withUnavailableCoins(lowerBound.add(Coins.ofSatoshis(1))), lowerBound);
        }

        @Test
        void updates_lower_bound_if_below_upper_bound() {
            Coins lowerBound = Coins.ofSatoshis(300);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withUnavailableCoins(lowerBound), oneSatLessThan(lowerBound));
        }

        @Test
        void unavailable_far_below_lower_bound_with_amount_in_flight() {
            LiquidityBounds initial =
                    new LiquidityBounds(Coins.ofSatoshis(300), null, Coins.ofSatoshis(100));
            Coins unavailableAmount = Coins.ofSatoshis(10);
            LiquidityBounds updated = initial.withUnavailableCoins(unavailableAmount).orElseThrow();
            assertLowerBound(updated.withAdditionalInFlight(Coins.ofSatoshis(-100)), oneSatLessThan(unavailableAmount));
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class WithMovedCoins {
        @Test
        void updates_lower_bound() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withMovedCoins(Coins.ofSatoshis(60)), Coins.ofSatoshis(40));
        }

        @Test
        void resets_lower_bound_if_moved_more_than_lower_bound() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withMovedCoins(Coins.ofSatoshis(110)), Coins.NONE);
        }
    }

    @Nested
    @SuppressWarnings(CLASS_CAN_BE_STATIC)
    class WithAdditionalInFlight {
        @Test
        void upper_bound_is_reduced_by_amount_in_flight() {
            /*
             * It is possible that the sats never reach this channel (because the payment fails on the way), but it
             * is also possible that the in-flight amount actually blocks otherwise available liquidity.
             *
             * If we do not subtract the in-flight amount form the upper bound, temporary channel failures would not
             * lower the upper bound accordingly. As such, the algorithm might try this edge again and again.
             *
             * Instead, if we subtract the in-flight amount, other edges can be attempted. As soon as the in-flight
             * amount is removed, this edge might be tried again.
             */

            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertUpperBound(initial.withAdditionalInFlight(Coins.ofSatoshis(40)), Coins.ofSatoshis(60));
        }

        @Test
        void two_in_flight_attempts_and_one_fails() {
            /*
             * Let's assume we start one attempt with 21sat and another with 100sat, so that 121sat are in flight.
             * If the 21sat payment fails, we first remove the 21sat from the in-flight amount. If the remaining 100sat
             * actually are pending, the true upper bound should be set to 120sat. Because the 100sat need to be
             * considered, the returned upper bound needs to be 20sat, though.
             *
             * However, if the 100sat payment never reaches this channel, a failure of 21sat should result in an upper
             * bound of 20sat.
             *
             * In both cases a value of 20sat needs to be returned as the upper bound while the 100sat are in-flight.
             * Once the 100sat are removed from the in-flight amount (without causing a failure in this channel),
             * we can return an upper bound of 120.
             */
            LiquidityBounds initial = withInFlight(Coins.ofSatoshis(100));
            Coins unavailableAmount = Coins.ofSatoshis(21);

            LiquidityBounds afterFailure = initial.withUnavailableCoins(unavailableAmount).orElseThrow();
            assertThat(afterFailure.getUpperBound()).contains(Coins.ofSatoshis(20));

            LiquidityBounds withoutInFlight = afterFailure.withAdditionalInFlight(Coins.ofSatoshis(-100)).orElseThrow();
            assertThat(withoutInFlight.getUpperBound()).contains(Coins.ofSatoshis(120));
        }

        @Test
        void upper_bound_is_reduced_by_amount_in_flight_if_upper_bound_equals_in_flight_amount() {
            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertUpperBound(initial.withAdditionalInFlight(upperBound), Coins.NONE);
        }

        @Test
        void upper_bound_is_reduced_by_amount_in_flight_if_upper_bound_is_less_than_in_flight_amount() {
            // the 100 sats in flight might not reach this channel, so we should not lower the upper bound
            Coins upperBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withUpperBound(upperBound);
            assertUpperBound(initial.withAdditionalInFlight(Coins.ofSatoshis(101)), Coins.NONE);
        }

        @Test
        void lower_bound_is_reduced_by_amount_in_flight() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withAdditionalInFlight(Coins.ofSatoshis(40)), Coins.ofSatoshis(60));
        }

        @Test
        void lower_bound_is_reduced_by_amount_in_flight_if_lower_bound_equals_in_flight_amount() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withAdditionalInFlight(lowerBound), Coins.NONE);
        }

        @Test
        void lower_bound_is_zero_if_lower_bound_is_below_in_flight() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = withLowerBound(lowerBound);
            assertLowerBound(initial.withAdditionalInFlight(Coins.ofSatoshis(101)), Coins.NONE);
        }

        @Test
        void in_flight_can_be_stacked() {
            LiquidityBounds initial =
                    new LiquidityBounds(Coins.ofSatoshis(100), null, Coins.ofSatoshis(60));
            assertLowerBound(initial.withAdditionalInFlight(Coins.ofSatoshis(10)), Coins.ofSatoshis(30));
        }

        @Test
        void in_flight_can_be_reversed() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = new LiquidityBounds(lowerBound, null, Coins.ofSatoshis(60));
            assertLowerBound(initial.withAdditionalInFlight(Coins.ofSatoshis(-60)), lowerBound);
        }

        @Test
        void remove_in_flight_without_bound_information() {
            LiquidityBounds initial = new LiquidityBounds(Coins.NONE, null, Coins.ofSatoshis(60));
            LiquidityBounds updated = initial.withAdditionalInFlight(Coins.ofSatoshis(-60)).orElseThrow();
            assertThat(updated).isSameAs(NO_INFORMATION);
        }

        @Test
        void in_flight_is_at_least_zero() {
            Coins lowerBound = Coins.ofSatoshis(100);
            LiquidityBounds initial = new LiquidityBounds(lowerBound, null, Coins.ofSatoshis(60));
            assertLowerBound(initial.withAdditionalInFlight(Coins.ofSatoshis(-61)), lowerBound);
        }
    }

    private void assertLowerBound(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<LiquidityBounds> liquidityBounds,
            Coins expectedLowerBound
    ) {
        assertThat(liquidityBounds.orElseThrow().getLowerBound()).isEqualTo(expectedLowerBound);
    }

    private void assertUpperBound(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<LiquidityBounds> liquidityBounds,
            Coins expectedUpperBound
    ) {
        assertThat(liquidityBounds.orElseThrow().getUpperBound()).contains(expectedUpperBound);
    }

    private Coins oneSatLessThan(Coins amount) {
        return amount.subtract(Coins.ofSatoshis(1));
    }

    private LiquidityBounds withLowerBound(Coins lowerBound) {
        return new LiquidityBounds(lowerBound, null, Coins.NONE);
    }

    private LiquidityBounds withUpperBound(Coins upperBound) {
        return new LiquidityBounds(Coins.NONE, upperBound, Coins.NONE);
    }

    private LiquidityBounds withInFlight(Coins inFlight) {
        return new LiquidityBounds(Coins.NONE, null, inFlight);
    }
}
