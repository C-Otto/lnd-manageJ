package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT_2;
import static org.assertj.core.api.Assertions.assertThat;

class FlowReportTest {
    @Test
    void forwardedSent() {
        assertThat(FLOW_REPORT.forwardedSent()).isEqualTo(Coins.ofSatoshis(1_050));
    }

    @Test
    void forwardedReceived() {
        assertThat(FLOW_REPORT.forwardedReceived()).isEqualTo(Coins.ofSatoshis(9_001));
    }

    @Test
    void forwardingFeesReceived() {
        assertThat(FLOW_REPORT.forwardingFeesReceived()).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void rebalanceSent() {
        assertThat(FLOW_REPORT.rebalanceSent()).isEqualTo(Coins.ofSatoshis(50));
    }

    @Test
    void rebalanceFeesSent() {
        assertThat(FLOW_REPORT.rebalanceFeesSent()).isEqualTo(Coins.ofMilliSatoshis(5));
    }

    @Test
    void rebalanceReceived() {
        assertThat(FLOW_REPORT.rebalanceReceived()).isEqualTo(Coins.ofSatoshis(51));
    }

    @Test
    void rebalanceSupportSent() {
        assertThat(FLOW_REPORT.rebalanceSupportSent()).isEqualTo(Coins.ofMilliSatoshis(123));
    }

    @Test
    void rebalanceSupportFeesSent() {
        assertThat(FLOW_REPORT.rebalanceSupportFeesSent()).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void rebalanceSupportReceived() {
        assertThat(FLOW_REPORT.rebalanceSupportReceived()).isEqualTo(Coins.ofMilliSatoshis(456));
    }

    @Test
    void receivedViaPayments() {
        assertThat(FLOW_REPORT.receivedViaPayments()).isEqualTo(Coins.ofMilliSatoshis(1_500));
    }

    @Test
    void totalSent() {
        assertThat(FLOW_REPORT.totalSent()).isEqualTo(Coins.ofMilliSatoshis(1_100_129));
    }

    @Test
    void totalReceived() {
        assertThat(FLOW_REPORT.totalReceived()).isEqualTo(Coins.ofMilliSatoshis(9_053_957));
    }

    @Test
    void empty() {
        assertThat(FlowReport.EMPTY).isEqualTo(new FlowReport(
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE,
                Coins.NONE
        ));
    }

    @Test
    void add_empty_to_empty() {
        assertThat(FlowReport.EMPTY.add(FlowReport.EMPTY)).isEqualTo(FlowReport.EMPTY);
    }

    @Test
    void add() {
        assertThat(FLOW_REPORT.add(FLOW_REPORT_2)).isEqualTo(new FlowReport(
                Coins.ofSatoshis(1_051),
                Coins.ofSatoshis(9_003),
                Coins.ofMilliSatoshis(11),
                Coins.ofSatoshis(110),
                Coins.ofMilliSatoshis(9),
                Coins.ofSatoshis(112),
                Coins.ofMilliSatoshis(9_123),
                Coins.ofMilliSatoshis(3),
                Coins.ofMilliSatoshis(466),
                Coins.ofMilliSatoshis(1_501)
        ));
    }
}
