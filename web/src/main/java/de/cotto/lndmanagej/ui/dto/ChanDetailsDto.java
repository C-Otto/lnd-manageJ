package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.*;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.Set;

public record ChanDetailsDto(
        ChannelId channelId,
        Pubkey remotePubkey,
        String remoteAlias,
        OpenInitiator openInitiator,
        BalanceInformationDto balanceInformation,
        OnChainCostsDto onChainCosts,
        PoliciesDto policies,
        FeeReportDto feeReport,
        FlowReportDto flowReport,
        RebalanceReportDto rebalanceReport,
        Set<String> warnings
) {


}
