package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelDetails;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.OpenCloseStatus;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class ChannelDetailsService {
    private final OnChainCostService onChainCostService;
    private final RebalanceService rebalanceService;
    private final NodeService nodeService;
    private final BalanceService balanceService;
    private final PolicyService policyService;
    private final FeeService feeService;
    private final FlowService flowService;
    private final ChannelWarningsService channelWarningsService;

    public ChannelDetailsService(
            OnChainCostService onChainCostService,
            RebalanceService rebalanceService,
            NodeService nodeService,
            BalanceService balanceService,
            PolicyService policyService,
            FeeService feeService,
            FlowService flowService,
            ChannelWarningsService channelWarningsService
    ) {
        this.onChainCostService = onChainCostService;
        this.rebalanceService = rebalanceService;
        this.nodeService = nodeService;
        this.balanceService = balanceService;
        this.policyService = policyService;
        this.feeService = feeService;
        this.flowService = flowService;
        this.channelWarningsService = channelWarningsService;
    }

    public ChannelDetails getDetails(LocalChannel localChannel) {
        ChannelId channelId = localChannel.getId();
        Pubkey remotePubkey = localChannel.getRemotePubkey();
        CompletableFuture<String> remoteAlias = getAlias(remotePubkey);
        CompletableFuture<BalanceInformation> balanceInformation = getBalanceInformation(channelId);
        CompletableFuture<OnChainCosts> onChainCosts = getOnChainCosts(channelId);
        CompletableFuture<Policies> policies = getPoliciesForChannel(localChannel);
        CompletableFuture<FeeReport> feeReport = getFeeReport(channelId);
        CompletableFuture<FlowReport> flowReport = getFlowReport(channelId);
        CompletableFuture<RebalanceReport> rebalanceReport = getRebalanceReport(localChannel);
        CompletableFuture<ChannelWarnings> channelWarnings = getChannelWarnings(localChannel);
        try {
            return new ChannelDetails(
                    localChannel,
                    remoteAlias.get(),
                    balanceInformation.get(),
                    onChainCosts.get(),
                    policies.get(),
                    feeReport.get(),
                    flowReport.get(),
                    rebalanceReport.get(),
                    channelWarnings.get()
            );
        } catch (InterruptedException | ExecutionException exception) {
            throw new IllegalStateException("Unable to compute channel details for " + channelId, exception);
        }
    }

    private CompletableFuture<ChannelWarnings> getChannelWarnings(LocalChannel localChannel) {
        return CompletableFuture.supplyAsync(() -> channelWarningsService.getChannelWarnings(localChannel.getId()));
    }

    private CompletableFuture<RebalanceReport> getRebalanceReport(LocalChannel localChannel) {
        return CompletableFuture.supplyAsync(() -> rebalanceService.getReportForChannel(localChannel.getId()));
    }

    private CompletableFuture<FeeReport> getFeeReport(ChannelId channelId) {
        return CompletableFuture.supplyAsync(() -> feeService.getFeeReportForChannel(channelId));
    }

    private CompletableFuture<FlowReport> getFlowReport(ChannelId channelId) {
        return CompletableFuture.supplyAsync(() -> flowService.getFlowReportForChannel(channelId));
    }

    private CompletableFuture<OnChainCosts> getOnChainCosts(ChannelId channelId) {
        return CompletableFuture.supplyAsync(() -> onChainCostService.getOnChainCostsForChannelId(channelId));
    }

    private CompletableFuture<String> getAlias(Pubkey remotePubkey) {
        return CompletableFuture.supplyAsync(() -> nodeService.getAlias(remotePubkey));
    }

    private CompletableFuture<BalanceInformation> getBalanceInformation(ChannelId channelId) {
        return CompletableFuture.supplyAsync(
                () -> balanceService.getBalanceInformation(channelId).orElse(BalanceInformation.EMPTY)
        );
    }

    private CompletableFuture<Policies> getPoliciesForChannel(LocalChannel channel) {
        if (channel.getStatus().openCloseStatus() != OpenCloseStatus.OPEN) {
            return CompletableFuture.completedFuture(Policies.UNKNOWN);
        }
        return CompletableFuture.supplyAsync(() -> policyService.getPolicies(channel.getId()));
    }
}
