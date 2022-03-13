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