package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FlowReport;

public record FlowReportDto(
        String forwardedSentMilliSat,
        String forwardedReceivedMilliSat,
        String forwardingFeesReceivedMilliSat,
        String rebalanceSentMilliSat,
        String rebalanceFeesSentMilliSat,
        String rebalanceReceivedMilliSat,
        String rebalanceSupportSentMilliSat,
        String rebalanceSupportFeesSentMilliSat,
        String rebalanceSupportReceivedMilliSat,
        String totalSentMilliSat,
        String totalReceivedMilliSat
) {
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public FlowReportDto(
            Coins forwardedSent,
            Coins forwardedReceived,
            Coins forwardingFeesReceived,
            Coins rebalanceSent,
            Coins rebalanceFeesSent,
            Coins rebalanceReceived,
            Coins rebalanceSupportSent,
            Coins rebalanceSupportFeesSent,
            Coins rebalanceSupportReceived,
            Coins totalSent,
            Coins totalReceived
    ) {
        this(
                String.valueOf(forwardedSent.milliSatoshis()),
                String.valueOf(forwardedReceived.milliSatoshis()),
                String.valueOf(forwardingFeesReceived.milliSatoshis()),
                String.valueOf(rebalanceSent.milliSatoshis()),
                String.valueOf(rebalanceFeesSent.milliSatoshis()),
                String.valueOf(rebalanceReceived.milliSatoshis()),
                String.valueOf(rebalanceSupportSent.milliSatoshis()),
                String.valueOf(rebalanceSupportFeesSent.milliSatoshis()),
                String.valueOf(rebalanceSupportReceived.milliSatoshis()),
                String.valueOf(totalSent.milliSatoshis()),
                String.valueOf(totalReceived.milliSatoshis())
        );
    }

    public static FlowReportDto createFromModel(FlowReport flowReport) {
        return new FlowReportDto(
                flowReport.forwardedSent(),
                flowReport.forwardedReceived(),
                flowReport.forwardingFeesReceived(),
                flowReport.rebalanceSent(),
                flowReport.rebalanceFeesSent(),
                flowReport.rebalanceReceived(),
                flowReport.rebalanceSupportSent(),
                flowReport.rebalanceSupportFeesSent(),
                flowReport.rebalanceSupportReceived(),
                flowReport.totalSent(),
                flowReport.totalReceived()
        );
    }
}
