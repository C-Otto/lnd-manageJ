package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class RebalanceReportTest {

    @Test
    void empty() {
        RebalanceReport empty =
                new RebalanceReport(Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE);
        assertThat(RebalanceReport.EMPTY).isEqualTo(empty);
    }

    @Test
    void sourceCost() {
        assertThat(REBALANCE_REPORT.sourceCost()).isEqualTo(Coins.ofSatoshis(1000));
    }

    @Test
    void sourceAmount() {
        assertThat(REBALANCE_REPORT.sourceAmount()).isEqualTo(Coins.ofSatoshis(665));
    }

    @Test
    void targetCost() {
        assertThat(REBALANCE_REPORT.targetCost()).isEqualTo(Coins.ofSatoshis(2000));
    }

    @Test
    void targetAmount() {
        assertThat(REBALANCE_REPORT.targetAmount()).isEqualTo(Coins.ofSatoshis(991));
    }

    @Test
    void supportAsSourceAmount() {
        assertThat(REBALANCE_REPORT.supportAsSourceAmount()).isEqualTo(Coins.ofSatoshis(100));
    }

    @Test
    void supportAsTargetAmount() {
        assertThat(REBALANCE_REPORT.supportAsTargetAmount()).isEqualTo(Coins.ofSatoshis(200));
    }
}