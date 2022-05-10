package de.cotto.lndmanagej.ui.model;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.FeeReportFixtures;
import de.cotto.lndmanagej.model.FlowReportFixtures;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.RebalanceReportFixtures;
import de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public class ChannelDetailsDtoFixture {

    public static final ChannelDetailsDto CHANNEL_DETAILS_DTO = new ChannelDetailsDto(
            CHANNEL_ID,
            PUBKEY,
            "Albert",
            OpenInitiator.LOCAL,
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
            OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            FeeReportDto.createFromModel(FeeReportFixtures.FEE_REPORT),
            FlowReportDto.createFromModel(FlowReportFixtures.FLOW_REPORT),
            RebalanceReportDto.createFromModel(RebalanceReportFixtures.REBALANCE_REPORT),
            ChannelWarningsFixtures.CHANNEL_WARNINGS.descriptions());
}
