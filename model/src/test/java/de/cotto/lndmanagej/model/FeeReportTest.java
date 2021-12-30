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

    @Test
    void empty() {
        assertThat(FeeReport.EMPTY).isEqualTo(new FeeReport(Coins.NONE, Coins.NONE));
    }

    @Test
    void add_empty_to_empty() {
        assertThat(FeeReport.EMPTY.add(FeeReport.EMPTY)).isEqualTo(FeeReport.EMPTY);
    }

    @Test
    void add() {
        assertThat(FEE_REPORT.add(new FeeReport(Coins.ofSatoshis(1), Coins.ofSatoshis(2))))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(2_234), Coins.ofMilliSatoshis(2_567)));
    }
}