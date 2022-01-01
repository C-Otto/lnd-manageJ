package de.cotto.lndmanagej.model;

public class FlowReportFixtures {
    public static final FlowReport FLOW_REPORT = new FlowReport(
            Coins.ofSatoshis(1_050),
            Coins.ofSatoshis(9_001),
            Coins.ofMilliSatoshis(1),
            Coins.ofSatoshis(50),
            Coins.ofMilliSatoshis(5),
            Coins.ofSatoshis(51),
            Coins.ofMilliSatoshis(123),
            Coins.ofMilliSatoshis(1),
            Coins.ofMilliSatoshis(456)
    );
    public static final FlowReport FLOW_REPORT_2 = new FlowReport(
            Coins.ofSatoshis(1),
            Coins.ofSatoshis(2),
            Coins.ofMilliSatoshis(10),
            Coins.ofSatoshis(60),
            Coins.ofMilliSatoshis(4),
            Coins.ofSatoshis(61),
            Coins.ofSatoshis(9),
            Coins.ofMilliSatoshis(2),
            Coins.ofMilliSatoshis(10)
    );
}
