package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.RebalanceReport;

public record RebalanceReportDto(String sourceCosts, String sourceAmount, String targetCosts, String targetAmount) {
    public RebalanceReportDto(Coins sourceCost, Coins sourceAmount, Coins targetCost, Coins targetAmount) {
        this(
                toMilliSatoshisString(sourceCost),
                toMilliSatoshisString(sourceAmount),
                toMilliSatoshisString(targetCost),
                toMilliSatoshisString(targetAmount)
        );
    }

    public static RebalanceReportDto createFromModel(RebalanceReport rebalanceReport) {
        return new RebalanceReportDto(
                rebalanceReport.sourceCost(),
                rebalanceReport.sourceAmount(),
                rebalanceReport.targetCost(),
                rebalanceReport.targetAmount()
        );
    }

    private static String toMilliSatoshisString(Coins coins) {
        return String.valueOf(coins.milliSatoshis());
    }
}
