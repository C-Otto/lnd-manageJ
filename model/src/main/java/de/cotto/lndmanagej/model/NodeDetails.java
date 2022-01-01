package de.cotto.lndmanagej.model;

import java.util.List;

public record NodeDetails(
        Pubkey pubkey,
        String alias,
        List<ChannelId> channels,
        List<ChannelId> closedChannels,
        List<ChannelId> waitingCloseChannels,
        List<ChannelId> pendingForceClosingChannels,
        OnChainCosts onChainCosts,
        BalanceInformation balanceInformation,
        OnlineReport onlineReport,
        FeeReport feeReport,
        FlowReport flowReport,
        RebalanceReport rebalanceReport,
        NodeWarnings nodeWarnings
) {
}
