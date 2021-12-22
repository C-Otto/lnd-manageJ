package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.RebalanceReport;

public record RebalanceReportDto(
        String sourceCosts,
        String sourceAmount,
        String targetCosts,
        String targetAmount,
        String supportAsSourceAmount,
        String supportAsTargetAmount
) {
    public RebalanceReportDto(
            Coins sourceCost,
            Coins sourceAmount,
            Coins targetCost,
            Coins targetAmount,
            Coins supportAsSourceAmount,
            Coins supportAsTargetAmount
    ) {
        this(
                toMilliSatoshisString(sourceCost),
                toMilliSatoshisString(sourceAmount),
                toMilliSatoshisString(targetCost),
                toMilliSatoshisString(targetAmount),
                toMilliSatoshisString(supportAsSourceAmount),
                toMilliSatoshisString(supportAsTargetAmount)
        );
    }

    public static RebalanceReportDto createFromModel(RebalanceReport rebalanceReport) {
        return new RebalanceReportDto(
                rebalanceReport.sourceCost(),
                rebalanceReport.sourceAmount(),
                rebalanceReport.targetCost(),
                rebalanceReport.targetAmount(),
                rebalanceReport.supportAsSourceAmount(),
                rebalanceReport.supportAsTargetAmount()
        );
    }

    private static String toMilliSatoshisString(Coins coins) {
        return String.valueOf(coins.milliSatoshis());
    }
}
