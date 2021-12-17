package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelDetails;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenCloseStatus;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

@Component
public class ChannelDetailsService {
    private final OnChainCostService onChainCostService;
    private final RebalanceService rebalanceService;
    private final NodeService nodeService;
    private final BalanceService balanceService;
    private final PolicyService policyService;
    private final FeeService feeService;

    public ChannelDetailsService(
            OnChainCostService onChainCostService,
            RebalanceService rebalanceService,
            NodeService nodeService,
            BalanceService balanceService,
            PolicyService policyService,
            FeeService feeService
    ) {
        this.onChainCostService = onChainCostService;
        this.rebalanceService = rebalanceService;
        this.nodeService = nodeService;
        this.balanceService = balanceService;
        this.policyService = policyService;
        this.feeService = feeService;
    }

    public ChannelDetails getDetails(LocalChannel localChannel) {
        Pubkey remotePubkey = localChannel.getRemotePubkey();
        String remoteAlias = nodeService.getAlias(remotePubkey);
        ChannelId channelId = localChannel.getId();
        return new ChannelDetails(
                localChannel,
                remoteAlias,
                getBalanceInformation(channelId),
                onChainCostService.getOnChainCostsForChannelId(channelId),
                getPoliciesForChannel(localChannel),
                feeService.getFeeReportForChannel(channelId),
                rebalanceService.getReportForChannel(localChannel.getId())
        );
    }

    private BalanceInformation getBalanceInformation(ChannelId channelId) {
        return balanceService.getBalanceInformation(channelId)
                .orElse(BalanceInformation.EMPTY);
    }

    private Policies getPoliciesForChannel(LocalChannel channel) {
        if (channel.getStatus().openCloseStatus() != OpenCloseStatus.OPEN) {
            return Policies.UNKNOWN;
        }
        return policyService.getPolicies(channel.getId());
    }
}
