package de.cotto.lndmanagej.model;

import de.cotto.lndmanagej.model.warnings.NodeWarnings;

import java.util.List;
import java.util.Optional;

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
        NodeWarnings warnings,
        Optional<PeerRating> rating
) {
}
