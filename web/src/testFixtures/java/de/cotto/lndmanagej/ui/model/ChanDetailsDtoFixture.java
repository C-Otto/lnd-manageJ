package de.cotto.lndmanagej.ui.model;

import de.cotto.lndmanagej.controller.dto.*;
import de.cotto.lndmanagej.model.*;
import de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;

public class ChanDetailsDtoFixture {

    public static final ChanDetailsDto CHAN_DETAILS_DTO = new ChanDetailsDto(
            ChannelIdFixtures.CHANNEL_ID,
            PubkeyFixtures.PUBKEY,
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
