package de.cotto.lndmanagej.model;

import de.cotto.lndmanagej.model.warnings.ChannelWarnings;

import java.util.Optional;

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
        Optional<ChannelRating> rating
) {
}
