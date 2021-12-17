package de.cotto.lndmanagej.model;

public class RebalanceReportFixtures {
    public static final RebalanceReport REBALANCE_REPORT = new RebalanceReport(
            Coins.ofSatoshis(1000),
            Coins.ofSatoshis(665),
            Coins.ofSatoshis(2000),
            Coins.ofSatoshis(991)
    );
}
