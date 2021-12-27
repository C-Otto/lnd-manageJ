package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.NodeDetails;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class NodeDetailsService {
    private final ChannelService channelService;
    private final NodeService nodeService;
    private final OnChainCostService onChainCostService;
    private final BalanceService balanceService;
    private final FeeService feeService;
    private final RebalanceService rebalanceService;
    private final OnlinePeersService onlinePeersService;
    private final NodeWarningsService warningsService;

    public NodeDetailsService(
            ChannelService channelService,
            NodeService nodeService,
            OnChainCostService onChainCostService,
            BalanceService balanceService,
            FeeService feeService,
            RebalanceService rebalanceService,
            OnlinePeersService onlinePeersService,
            NodeWarningsService warningsService
    ) {
        this.channelService = channelService;
        this.nodeService = nodeService;
        this.onChainCostService = onChainCostService;
        this.balanceService = balanceService;
        this.feeService = feeService;
        this.rebalanceService = rebalanceService;
        this.onlinePeersService = onlinePeersService;
        this.warningsService = warningsService;
    }

    public NodeDetails getDetails(Pubkey pubkey) {
        CompletableFuture<Node> node = getNode(pubkey);
        CompletableFuture<OnlineReport> onlineReport = node.thenApply(onlinePeersService::getOnlineReport);
        CompletableFuture<OnChainCosts> onChainCosts = getOnChainCosts(pubkey);
        CompletableFuture<BalanceInformation> balanceInformation = getBalanceInformation(pubkey);
        CompletableFuture<FeeReport> feeReport = getFeeReport(pubkey);
        CompletableFuture<RebalanceReport> rebalanceReport = getRebalanceReport(pubkey);
        CompletableFuture<NodeWarnings> nodeWarnings = getNodeWarnings(pubkey);
        List<ChannelId> openChannelIds =
                getSortedChannelIds(channelService.getOpenChannelsWith(pubkey));
        List<ChannelId> closedChannelIds =
                getSortedChannelIds(channelService.getClosedChannelsWith(pubkey));
        List<ChannelId> waitingCloseChannelIds =
                getSortedChannelIds(channelService.getWaitingCloseChannelsWith(pubkey));
        List<ChannelId> forceClosingChannelIds =
                getSortedChannelIds(channelService.getForceClosingChannelsWith(pubkey));
        try {
            return new NodeDetails(
                    pubkey,
                    node.get().alias(),
                    openChannelIds,
                    closedChannelIds,
                    waitingCloseChannelIds,
                    forceClosingChannelIds,
                    onChainCosts.get(),
                    balanceInformation.get(),
                    onlineReport.get(),
                    feeReport.get(),
                    rebalanceReport.get(),
                    nodeWarnings.get()
            );
        } catch (InterruptedException | ExecutionException exception) {
            throw new IllegalStateException("Unable to compute node details for " + pubkey, exception);
        }
    }

    private CompletableFuture<Node> getNode(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> nodeService.getNode(pubkey));
    }

    private CompletableFuture<OnChainCosts> getOnChainCosts(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> onChainCostService.getOnChainCostsForPeer(pubkey));
    }

    private CompletableFuture<FeeReport> getFeeReport(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> feeService.getFeeReportForPeer(pubkey));
    }

    private CompletableFuture<RebalanceReport> getRebalanceReport(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> rebalanceService.getReportForPeer(pubkey));
    }

    private CompletableFuture<NodeWarnings> getNodeWarnings(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> warningsService.getNodeWarnings(pubkey));
    }

    private CompletableFuture<BalanceInformation> getBalanceInformation(Pubkey pubkey) {
        return CompletableFuture.supplyAsync(() -> balanceService.getBalanceInformationForPeer(pubkey));
    }

    private List<ChannelId> getSortedChannelIds(Set<? extends Channel> channels) {
        return channels.stream()
                .map(Channel::getId)
                .sorted()
                .toList();
    }
}
