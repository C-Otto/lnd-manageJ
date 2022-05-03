package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LiquidityBoundsTest {
    private LiquidityBounds liquidityBounds = new LiquidityBounds();

    @Test
    void getLowerBound_initially_zero() {
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.NONE);
    }

    @Test
    void getLowerBound_after_available_update() {
        Coins amount = Coins.ofSatoshis(100);
        liquidityBounds.available(amount);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(amount);
    }

    @Test
    void getLowerBound_two_available_updates() {
        Coins amount1 = Coins.ofSatoshis(100);
        Coins amount2 = Coins.ofSatoshis(200);
        liquidityBounds.available(amount1);
        liquidityBounds.available(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(amount2);
    }

    @Test
    void getLowerBound_two_available_updates_reversed() {
        Coins amount1 = Coins.ofSatoshis(200);
        Coins amount2 = Coins.ofSatoshis(100);
        liquidityBounds.available(amount1);
        liquidityBounds.available(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(amount1);
    }

    @Test
    void getUpperBound_initially_unknown() {
        assertThat(liquidityBounds.getUpperBound()).isEmpty();
    }

    @Test
    void getUpperBound_after_unavailable_update() {
        Coins amount = Coins.ofSatoshis(200);
        liquidityBounds.unavailable(amount);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(amount));
    }

    @Test
    void getUpperBound_after_two_unavailable_updates() {
        Coins amount1 = Coins.ofSatoshis(200);
        Coins amount2 = Coins.ofSatoshis(100);
        liquidityBounds.unavailable(amount1);
        liquidityBounds.unavailable(amount2);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(amount2));
    }

    @Test
    void getUpperBound_after_unavailable_updates_reversed() {
        Coins amount1 = Coins.ofSatoshis(100);
        Coins amount2 = Coins.ofSatoshis(200);
        liquidityBounds.unavailable(amount1);
        liquidityBounds.unavailable(amount2);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(amount1));
    }

    @Test
    void available_update_invalidates_lower_upper_bound() {
        Coins amount1 = Coins.ofSatoshis(100);
        Coins amount2 = oneSatLessThan(amount1);
        liquidityBounds.unavailable(amount1);
        liquidityBounds.available(amount2);
        assertThat(liquidityBounds.getUpperBound()).isEmpty();
    }

    @Test
    void available_update_keeps_higher_upper_bound() {
        Coins amount1 = Coins.ofSatoshis(300);
        Coins amount2 = Coins.ofSatoshis(298);
        liquidityBounds.unavailable(amount1);
        liquidityBounds.available(amount2);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(amount1));
    }

    @Test
    void unavailable_update_updates_lower_bound() {
        Coins amount1 = Coins.ofSatoshis(300);
        Coins amount2 = Coins.ofSatoshis(301);
        liquidityBounds.available(amount1);
        liquidityBounds.unavailable(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(oneSatLessThan(amount2));
    }

    @Test
    void unavailable_what_is_assumed_to_be_available() {
        Coins amount1 = Coins.ofSatoshis(300);
        Coins amount2 = Coins.ofSatoshis(300);
        liquidityBounds.available(amount1);
        liquidityBounds.unavailable(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(oneSatLessThan(amount2));
    }

    @Test
    void unavailable_more_than_available() {
        Coins amount1 = Coins.ofSatoshis(300);
        Coins amount2 = Coins.ofSatoshis(500);
        liquidityBounds.available(amount1);
        liquidityBounds.unavailable(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(amount1);
    }

    @Test
    void move_updates_lower_bound() {
        Coins amount1 = Coins.ofSatoshis(100);
        Coins amount2 = Coins.ofSatoshis(60);
        liquidityBounds.available(amount1);
        liquidityBounds.move(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.ofSatoshis(40));
    }

    @Test
    void move_more_than_lower_bound() {
        Coins amount1 = Coins.ofSatoshis(100);
        Coins amount2 = Coins.ofSatoshis(200);
        liquidityBounds.available(amount1);
        liquidityBounds.move(amount2);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.NONE);
    }

    @Test
    void available_forgets_lower_bound_after_one_hour() {
        Coins higherButOld = Coins.ofSatoshis(200);
        Coins lowerMoreRecent = Coins.ofSatoshis(100);
        liquidityBounds.available(higherButOld);
        liquidityBounds.setLowerBoundLastUpdate(oneHourAgo());

        liquidityBounds.available(lowerMoreRecent);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(lowerMoreRecent);
    }

    @Test
    void available_forgets_lower_bound_after_customized_time() {
        Duration maxAge = Duration.ofMinutes(30);
        liquidityBounds = new LiquidityBounds(maxAge);

        Coins higherButOld = Coins.ofSatoshis(200);
        Coins lowerMoreRecent = Coins.ofSatoshis(100);
        liquidityBounds.available(higherButOld);
        liquidityBounds.setLowerBoundLastUpdate(Instant.now().minus(maxAge));

        liquidityBounds.available(lowerMoreRecent);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(lowerMoreRecent);
    }

    @Test
    void available_updates_last_update_of_lower_bound() {
        liquidityBounds.setLowerBoundLastUpdate(oneHourAgo());

        Coins lowerBound = Coins.ofSatoshis(50);
        liquidityBounds.available(lowerBound);
        assertThat(liquidityBounds.getLowerBound()).isEqualTo(lowerBound);
    }

    @Test
    void unavailable_forgets_upper_bound_after_one_hour() {
        liquidityBounds.unavailable(Coins.ofSatoshis(100));
        liquidityBounds.setUpperBoundLastUpdate(oneHourAgo());

        Coins moreRecentAmount = Coins.ofSatoshis(200);
        liquidityBounds.unavailable(moreRecentAmount);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(moreRecentAmount));
    }

    @Test
    void unavailable_updates_last_update_of_upper_bound() {
        liquidityBounds.setUpperBoundLastUpdate(oneHourAgo());

        Coins moreRecentValue = Coins.ofSatoshis(100);
        liquidityBounds.unavailable(moreRecentValue);
        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(moreRecentValue));
    }

    @Test
    void getLowerBound_does_not_return_old_value() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.setLowerBoundLastUpdate(oneHourAgo());

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.NONE);
    }

    @Test
    void getLowerBound_value_is_kept_for_old_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        liquidityBounds.available(amount);
        liquidityBounds.setUpperBoundLastUpdate(oneHourAgo());

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(amount);
    }

    @Test
    void getUpperBound_does_not_return_old_value() {
        liquidityBounds.unavailable(Coins.ofSatoshis(100));
        liquidityBounds.setUpperBoundLastUpdate(oneHourAgo());

        assertThat(liquidityBounds.getUpperBound()).isEmpty();
    }

    @Test
    void getUpperBound_value_is_kept_for_old_lower_bound() {
        Coins amount = Coins.ofSatoshis(100);
        liquidityBounds.unavailable(amount);
        liquidityBounds.setLowerBoundLastUpdate(oneHourAgo());

        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(amount));
    }

    @Test
    void upper_bound_is_reduced_by_amount_in_flight() {
        liquidityBounds.unavailable(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(40));

        assertThat(liquidityBounds.getUpperBound()).contains(oneSatLessThan(Coins.ofSatoshis(60)));
    }

    @Test
    void lower_bound_is_reduced_by_amount_in_flight() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(40));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.ofSatoshis(60));
    }

    @Test
    void available_lower_bound_in_flight() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(100));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.NONE);
    }

    @Test
    void more_than_available_lower_bound_in_flight() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(101));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.NONE);
    }

    @Test
    void available_upper_bound_in_flight() {
        liquidityBounds.unavailable(Coins.ofSatoshis(101));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(100));

        assertThat(liquidityBounds.getUpperBound()).contains(Coins.NONE);
    }

    @Test
    void more_than_available_upper_bound_in_flight() {
        liquidityBounds.unavailable(Coins.ofSatoshis(101));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(101));

        assertThat(liquidityBounds.getUpperBound()).contains(Coins.NONE);
    }

    @Test
    void in_flight_can_be_stacked() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(40));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(20));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.ofSatoshis(40));
    }

    @Test
    void in_flight_can_be_reversed() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(40));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(-40));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.ofSatoshis(100));
    }

    @Test
    void in_flight_at_least_zero() {
        liquidityBounds.available(Coins.ofSatoshis(100));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(40));
        liquidityBounds.addAsInFlight(Coins.ofSatoshis(-50));

        assertThat(liquidityBounds.getLowerBound()).isEqualTo(Coins.ofSatoshis(100));
    }

    private Instant oneHourAgo() {
        return Instant.now().minus(1, ChronoUnit.HOURS);
    }

    private Coins oneSatLessThan(Coins amount) {
        return amount.subtract(Coins.ofSatoshis(1));
    }
}
