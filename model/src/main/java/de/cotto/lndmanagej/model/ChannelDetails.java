package de.cotto.lndmanagej.model;

import de.cotto.lndmanagej.model.warnings.ChannelWarnings;

public record ChannelDetails(
        LocalChannel localChannel,
        String remoteAlias,
        BalanceInformation balanceInformation,
        OnChainCosts onChainCosts,
        PoliciesForLocalChannel policies,
        FeeReport feeReport,
        FlowReport flowReport,
        RebalanceReport rebalanceReport,
        ChannelWarnings warnings,
        Rating rating
) {
}
