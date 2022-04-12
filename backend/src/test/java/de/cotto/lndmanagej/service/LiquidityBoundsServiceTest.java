package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidityBoundsServiceTest {
    @InjectMocks
    private LiquidityBoundsService liquidityBoundsService;

    @Mock
    private MissionControlService missionControlService;

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
    void getAssumedLiquidityLowerBound_defaults_to_none() {
        assertThat(liquidityBoundsService.getAssumedLiquidityLowerBound(PUBKEY, PUBKEY_2))
                .isEqualTo(Coins.NONE);
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
}
