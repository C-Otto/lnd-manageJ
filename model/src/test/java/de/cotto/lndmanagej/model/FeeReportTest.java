package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class FeeReportTest {
    @Test
    void earned() {
        assertThat(FEE_REPORT.earned()).isEqualTo(Coins.ofMilliSatoshis(1_234));
    }

    @Test
    void sourced() {
        assertThat(FEE_REPORT.sourced()).isEqualTo(Coins.ofMilliSatoshis(567));
    }
}