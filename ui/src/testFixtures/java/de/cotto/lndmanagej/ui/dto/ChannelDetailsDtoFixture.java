package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;

import static de.cotto.lndmanagej.controller.dto.ChannelStatusDtoFixture.CHANNEL_STATUS_PUBLIC_OPEN;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.CAPACITY_SAT;

public class ChannelDetailsDtoFixture {

    public static final ChannelDetailsDto CHANNEL_DETAILS_DTO = new ChannelDetailsDto(
            CHANNEL_ID,
            PUBKEY,
            "Albert",
            365,
            CHANNEL_STATUS_PUBLIC_OPEN,
            LOCAL,
            BALANCE_INFORMATION_MODEL,
            CAPACITY_SAT,
            OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            FeeReportDto.createFromModel(FEE_REPORT),
            FlowReportDto.createFromModel(FLOW_REPORT),
            RebalanceReportDto.createFromModel(REBALANCE_REPORT),
            CHANNEL_WARNINGS.descriptions()
    );
}
