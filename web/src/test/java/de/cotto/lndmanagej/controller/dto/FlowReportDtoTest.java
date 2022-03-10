package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class FlowReportDtoTest {

    private static final Coins FORWARDED_SENT = Coins.ofMilliSatoshis(1);
    private static final Coins FORWARDED_RECEIVED = Coins.ofMilliSatoshis(2);
    private static final Coins FORWARDING_FEES_RECEIVED = Coins.ofMilliSatoshis(3);
    private static final Coins REBALANCE_SENT = Coins.ofMilliSatoshis(4);
    private static final Coins REBALANCE_FEES_SENT = Coins.ofMilliSatoshis(5);
    private static final Coins REBALANCE_RECEIVED = Coins.ofMilliSatoshis(6);
    private static final Coins REBALANCE_SUPPORT_SENT = Coins.ofMilliSatoshis(7);
    private static final Coins REBALANCE_SUPPORT_FEES_SENT = Coins.ofMilliSatoshis(8);
    private static final Coins REBALANCE_SUPPORT_RECEIVED = Coins.ofMilliSatoshis(9);
    private static final Coins TOTAL_SENT = Coins.ofMilliSatoshis(10);
    private static final Coins TOTAL_RECEIVED = Coins.ofMilliSatoshis(11);

    private static final FlowReportDto FLOW_REPORT_DTO =
            new FlowReportDto(
                    FORWARDED_SENT,
                    FORWARDED_RECEIVED,
                    FORWARDING_FEES_RECEIVED,
                    REBALANCE_SENT,
                    REBALANCE_FEES_SENT,
                    REBALANCE_RECEIVED,
                    REBALANCE_SUPPORT_SENT,
                    REBALANCE_SUPPORT_FEES_SENT,
                    REBALANCE_SUPPORT_RECEIVED,
                    TOTAL_SENT,
                    TOTAL_RECEIVED
            );

    @Test
    void forwardedSent() {
        assertThat(FLOW_REPORT_DTO.forwardedSentMilliSat())
                .isEqualTo(String.valueOf(FORWARDED_SENT.milliSatoshis()));
    }

    @Test
    void forwardedReceived() {
        assertThat(FLOW_REPORT_DTO.forwardedReceivedMilliSat())
                .isEqualTo(String.valueOf(FORWARDED_RECEIVED.milliSatoshis()));
    }

    @Test
    void forwardingFeesReceived() {
        assertThat(FLOW_REPORT_DTO.forwardingFeesReceivedMilliSat())
                .isEqualTo(String.valueOf(FORWARDING_FEES_RECEIVED.milliSatoshis()));
    }

    @Test
    void rebalanceSent() {
        assertThat(FLOW_REPORT_DTO.rebalanceSentMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_SENT.milliSatoshis()));
    }

    @Test
    void rebalanceFeesSent() {
        assertThat(FLOW_REPORT_DTO.rebalanceFeesSentMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_FEES_SENT.milliSatoshis()));
    }

    @Test
    void rebalanceReceived() {
        assertThat(FLOW_REPORT_DTO.rebalanceReceivedMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_RECEIVED.milliSatoshis()));
    }

    @Test
    void rebalanceSupportSent() {
        assertThat(FLOW_REPORT_DTO.rebalanceSupportSentMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_SUPPORT_SENT.milliSatoshis()));
    }

    @Test
    void rebalanceSupportFeesSent() {
        assertThat(FLOW_REPORT_DTO.rebalanceSupportFeesSentMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_SUPPORT_FEES_SENT.milliSatoshis()));
    }

    @Test
    void rebalanceSupportReceived() {
        assertThat(FLOW_REPORT_DTO.rebalanceSupportReceivedMilliSat())
                .isEqualTo(String.valueOf(REBALANCE_SUPPORT_RECEIVED.milliSatoshis()));
    }

    @Test
    void totalSent() {
        assertThat(FLOW_REPORT_DTO.totalSentMilliSat())
                .isEqualTo(String.valueOf(TOTAL_SENT.milliSatoshis()));
    }

    @Test
    void totalReceived() {
        assertThat(FLOW_REPORT_DTO.totalReceivedMilliSat())
                .isEqualTo(String.valueOf(TOTAL_RECEIVED.milliSatoshis()));
    }

    @Test
    void createFromModel() {
        assertThat(FlowReportDto.createFromModel(FLOW_REPORT)).isEqualTo(new FlowReportDto(
                "1050000",
                "9001000",
                "1",
                "50000",
                "5",
                "51000",
                "123",
                "1",
                "456",
                "1100129",
                "9052457"
        ));
    }
}