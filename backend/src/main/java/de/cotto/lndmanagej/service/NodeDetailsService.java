package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.NodeDetails;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class NodeDetailsService {
    private final ChannelService channelService;
    private final NodeService nodeService;
    private final OnChainCostService onChainCostService;
    private final BalanceService balanceService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;

    public NodeDetailsService(
            ChannelService channelService,
            NodeService nodeService,
            OnChainCostService onChainCostService,
            BalanceService balanceService,
            FeeService feeService,
            RebalanceService rebalanceService
    ) {
        this.channelService = channelService;
        this.nodeService = nodeService;
        this.onChainCostService = onChainCostService;
        this.balanceService = balanceService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
    }

    public NodeDetails getDetails(Pubkey pubkey) {
        Node node = nodeService.getNode(pubkey);
        OnChainCosts onChainCosts = onChainCostService.getOnChainCostsForPeer(pubkey);
        BalanceInformation balanceInformation = balanceService.getBalanceInformationForPeer(pubkey);
        return new NodeDetails(
                pubkey,
                node.alias(),
                getSortedChannelIds(channelService.getOpenChannelsWith(pubkey)),
                getSortedChannelIds(channelService.getClosedChannelsWith(pubkey)),
                getSortedChannelIds(channelService.getWaitingCloseChannelsWith(pubkey)),
                getSortedChannelIds(channelService.getForceClosingChannelsWith(pubkey)),
                onChainCosts,
                balanceInformation,
                node.online(),
                feeService.getFeeReportForPeer(pubkey),
                rebalanceService.getReportForPeer(pubkey)
        );
    }

    private List<ChannelId> getSortedChannelIds(Set<? extends Channel> channels) {
        return channels.stream()
                .map(Channel::getId)
                .sorted()
                .toList();
    }
}
