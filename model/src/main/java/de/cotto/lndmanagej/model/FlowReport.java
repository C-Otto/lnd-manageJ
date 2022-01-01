package de.cotto.lndmanagej.model;

public record FlowReport(
        Coins forwardedSent,
        Coins forwardedReceived,
        Coins forwardingFeesReceived,
        Coins rebalanceSent,
        Coins rebalanceFeesSent,
        Coins rebalanceReceived,
        Coins rebalanceSupportSent,
        Coins rebalanceSupportFeesSent,
        Coins rebalanceSupportReceived
) {
    public static final FlowReport EMPTY = new FlowReport(
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE,
            Coins.NONE
    );

    public FlowReport add(FlowReport other) {
        return new FlowReport(
                forwardedSent.add(other.forwardedSent),
                forwardedReceived.add(other.forwardedReceived),
                forwardingFeesReceived.add(other.forwardingFeesReceived),
                rebalanceSent.add(other.rebalanceSent),
                rebalanceFeesSent.add(other.rebalanceFeesSent),
                rebalanceReceived.add(other.rebalanceReceived),
                rebalanceSupportSent.add(other.rebalanceSupportSent),
                rebalanceSupportFeesSent.add(other.rebalanceSupportFeesSent),
                rebalanceSupportReceived.add(other.rebalanceSupportReceived)
        );
    }

    public Coins totalSent() {
        return forwardedSent
                .add(rebalanceSent)
                .add(rebalanceFeesSent)
                .add(rebalanceSupportSent)
                .add(rebalanceSupportFeesSent);
    }

    public Coins totalReceived() {
        return forwardedReceived
                .add(forwardingFeesReceived)
                .add(rebalanceReceived)
                .add(rebalanceSupportReceived);
    }
}
