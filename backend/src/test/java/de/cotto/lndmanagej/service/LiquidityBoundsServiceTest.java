package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.LIQUIDITY_INFORMATION_MAX_AGE;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.USE_MISSION_CONTROL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidityBoundsServiceTest {
    @InjectMocks
    private LiquidityBoundsService liquidityBoundsService;

    @Mock
    private MissionControlService missionControlService;

    @Mock
    private ConfigurationService configurationService;

    @Test
    void getAssumedLiquidityUpperBound_unknown() {
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void getAssumedLiquidityUpperBound_from_mission_control() {
        when(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2))
                .thenReturn(Optional.of(Coins.ofSatoshis(123)));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(122));
    }

    @Test
    void getAssumedLiquidityUpperBound_mission_control_disabled() {
        when(configurationService.getBooleanValue(USE_MISSION_CONTROL)).thenReturn(Optional.of(false));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2)).isEmpty();
        verifyNoInteractions(missionControlService);
    }

    @Test
    void getAssumedLiquidityLowerBound_defaults_to_none() {
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void liquidity_information_uses_configured_max_age() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1));
        verify(configurationService).getIntegerValue(LIQUIDITY_INFORMATION_MAX_AGE);
    }

    @Test
    void markAsAvailable() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(100_000));
    }

    @Test
    void markAsAvailable_uses_maximum() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(120_000));
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(130_000));
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(110_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(130_000));
    }

    @Test
    void markAsMoved_removes_available_coins() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(110_000));
        liquidityBoundsService.markAsMoved(PUBKEY, PUBKEY_2, Coins.ofSatoshis(50_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(60_000));
    }

    @Test
    void markAsMoved_below_assumed_available() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1));
        liquidityBoundsService.markAsMoved(PUBKEY, PUBKEY_2, Coins.ofSatoshis(50_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void markAsUnavailable() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(50_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(49_999));
    }

    @Test
    void markAsUnavailable_uses_minimum() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(50_000));
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(40_000));
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(60_000));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(39_999));
    }

    @Test
    void markAsUnavailable_with_existing_mission_control_data() {
        when(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2))
                .thenReturn(Optional.of(Coins.ofSatoshis(123)));
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(99));
    }

    @Test
    void markAsAvailable_more_than_upper_bound() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(120));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void markAsAvailable_below_upper_bound() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(90));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(99));
    }

    @Test
    void markAsUnavailable_below_lower_bound() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(120));
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(99));
    }

    @Test
    void markAsInFlight_reduces_upper_bound() {
        liquidityBoundsService.markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(901));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .contains(Coins.ofSatoshis(800));
    }

    @Test
    void markAsInFlight_reduces_lower_bound() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(300));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(200));
    }

    @Test
    void markAsInFlight_can_be_reversed() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(300));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(-100));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(300));
    }

    @Test
    void markAsInFlight_can_be_stacked() {
        liquidityBoundsService.markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(300));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(40));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.ofSatoshis(160));
    }

    @Test
    void markAsInFlight_for_unknown_lower_bound() {
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void markAsInFlight_for_unknown_upper_bound() {
        liquidityBoundsService.markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        assertThat(liquidityBoundsService.getAssumedLiquidityUpperBound(PUBKEY, PUBKEY_2))
                .isEmpty();
    }
}
