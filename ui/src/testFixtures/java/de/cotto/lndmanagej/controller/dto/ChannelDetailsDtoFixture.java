package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;

public class ChannelDetailsDtoFixture {

    public static final ChannelDetailsDto CHANNEL_DETAILS_DTO = new ChannelDetailsDto(
            CHANNEL_ID,
            PUBKEY,
            "Albert",
            LOCAL,
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
            OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            FeeReportDto.createFromModel(FEE_REPORT),
            FlowReportDto.createFromModel(FLOW_REPORT),
            RebalanceReportDto.createFromModel(REBALANCE_REPORT),
            CHANNEL_WARNINGS.descriptions());
}
