package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeReportTest {

    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Test
    void earned() {
        assertThat(FEE_REPORT.earned()).isEqualTo(Coins.ofMilliSatoshis(1_234));
    }

    @Test
    void sourced() {
        assertThat(FEE_REPORT.sourced()).isEqualTo(Coins.ofMilliSatoshis(567));
    }
}