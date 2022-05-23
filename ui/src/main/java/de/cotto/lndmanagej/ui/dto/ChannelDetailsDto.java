package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelStatusDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.Set;

public record ChannelDetailsDto(
        ChannelId channelId,
        Pubkey remotePubkey,
        String remoteAlias,
        ChannelStatusDto channelStatus,
        OpenInitiator openInitiator,
        BalanceInformationDto balanceInformation,
        long capacitySat,
        OnChainCostsDto onChainCosts,
        PoliciesDto policies,
        FeeReportDto feeReport,
        FlowReportDto flowReport,
        RebalanceReportDto rebalanceReport,
        Set<String> warnings
) {

    public long getRoutableCapacity() {
        long outbound = Long.parseLong(balanceInformation.localBalanceSat());
        long inbound = Long.parseLong(balanceInformation.remoteBalanceSat());
        return outbound + inbound;
    }

    public double getInboundPercentage() {
        return 100 - getOutboundPercentage();
    }

    public double getOutboundPercentage() {
        long outbound = Long.parseLong(balanceInformation.localBalanceSat());
        return (1.0 * outbound / getRoutableCapacity()) * 100;
    }

}
