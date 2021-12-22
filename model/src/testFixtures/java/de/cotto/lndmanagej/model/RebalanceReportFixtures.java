package de.cotto.lndmanagej.model;

public class RebalanceReportFixtures {
    public static final RebalanceReport REBALANCE_REPORT = new RebalanceReport(
            Coins.ofSatoshis(1000),
            Coins.ofSatoshis(665),
            Coins.ofSatoshis(2000),
            Coins.ofSatoshis(991),
            Coins.ofSatoshis(100),
            Coins.ofSatoshis(200)
    );

    public static final RebalanceReport REBALANCE_REPORT_2 = new RebalanceReport(
            Coins.ofSatoshis(1001),
            Coins.ofSatoshis(666),
            Coins.ofSatoshis(2001),
            Coins.ofSatoshis(992),
            Coins.ofSatoshis(101),
            Coins.ofSatoshis(201)
    );
}
