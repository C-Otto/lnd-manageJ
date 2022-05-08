package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static de.cotto.lndmanagej.model.MissionControlEntryFixtures.FAILURE;
import static de.cotto.lndmanagej.model.MissionControlEntryFixtures.SUCCESS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class MissionControlEntryTest {

    private static final MissionControlEntry ENTRY =
            new MissionControlEntry(PUBKEY, PUBKEY_2, Coins.ofSatoshis(123), Instant.ofEpochSecond(100), false);

    @Test
    void success() {
        assertThat(SUCCESS.success()).isTrue();
        assertThat(FAILURE.success()).isFalse();
    }

    @Test
    void failure() {
        assertThat(SUCCESS.failure()).isFalse();
        assertThat(FAILURE.failure()).isTrue();
    }

    @Test
    void isAfter_same_value() {
        assertThat(ENTRY.isAfter(Instant.ofEpochSecond(100))).isFalse();
    }

    @Test
    void isAfter_strictly_after() {
        assertThat(ENTRY.isAfter(Instant.ofEpochSecond(99))).isTrue();
    }

    @Test
    void isAfter_strictly_before() {
        assertThat(ENTRY.isAfter(Instant.ofEpochSecond(101))).isFalse();
    }

    @Test
    void time() {
        assertThat(SUCCESS.time()).isBetween(Instant.now().minus(Duration.ofSeconds(10)), Instant.now());
    }

    @Test
    void amount() {
        assertThat(SUCCESS.amount()).isEqualTo(Coins.ofSatoshis(123));
    }

    @Test
    void source() {
        assertThat(SUCCESS.source()).isEqualTo(PUBKEY);
    }

    @Test
    void target() {
        assertThat(SUCCESS.target()).isEqualTo(PUBKEY_2);
    }
}
