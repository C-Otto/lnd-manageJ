package de.cotto.lndmanagej.model;

public record ChannelDetails(
        LocalChannel localChannel,
        String remoteAlias,
        BalanceInformation balanceInformation,
        OnChainCosts onChainCosts,
        Policies policies,
        FeeReport feeReport,
        FlowReport flowReport,
        RebalanceReport rebalanceReport
) {
}
