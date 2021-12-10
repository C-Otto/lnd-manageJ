package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.ResolutionFixtures.RESOLUTION;
import static de.cotto.lndmanagej.model.ResolutionFixtures.RESOLUTION_2;
import static org.assertj.core.api.Assertions.assertThat;

class ResolutionTest {

    @Test
    void sweepTransaction_empty() {
        assertThat(RESOLUTION_2.sweepTransaction()).isEmpty();
    }

    @Test
    void sweepTransaction() {
        assertThat(RESOLUTION.sweepTransaction()).contains(TRANSACTION_HASH_3);
    }
}