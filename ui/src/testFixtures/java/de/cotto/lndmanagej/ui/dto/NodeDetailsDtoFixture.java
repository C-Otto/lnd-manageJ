package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.PubkeyFixtures;

import java.util.List;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;

public class NodeDetailsDtoFixture {

    public static final NodeDetailsDto NODE_DETAILS_MODEL = new NodeDetailsDto(
            PubkeyFixtures.PUBKEY,
            "Albert",
            List.of(CHANNEL_ID),
            List.of(CHANNEL_ID),
            List.of(CHANNEL_ID),
            List.of(CHANNEL_ID),
            OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
            OnlineReportDto.createFromModel(ONLINE_REPORT),
            FeeReportDto.createFromModel(FEE_REPORT),
            FlowReportDto.createFromModel(FLOW_REPORT),
            RebalanceReportDto.createFromModel(REBALANCE_REPORT),
            CHANNEL_WARNINGS.descriptions());
}
