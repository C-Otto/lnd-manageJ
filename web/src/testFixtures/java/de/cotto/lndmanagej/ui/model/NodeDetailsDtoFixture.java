package de.cotto.lndmanagej.ui.model;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelIdFixtures;
import de.cotto.lndmanagej.model.FeeReportFixtures;
import de.cotto.lndmanagej.model.FlowReportFixtures;
import de.cotto.lndmanagej.model.OnlineReportFixtures;
import de.cotto.lndmanagej.model.PubkeyFixtures;
import de.cotto.lndmanagej.model.RebalanceReportFixtures;
import de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures;

import java.util.List;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;

public class NodeDetailsDtoFixture {

    public static final NodeDetailsDto NODE_DETAILS_DTO = new NodeDetailsDto(
            PUBKEY,
            "Albert",
            List.of(ChannelIdFixtures.CHANNEL_ID),
            List.of(ChannelIdFixtures.CHANNEL_ID),
            List.of(ChannelIdFixtures.CHANNEL_ID),
            List.of(ChannelIdFixtures.CHANNEL_ID),
            OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
            OnlineReportDto.createFromModel(ONLINE_REPORT),
            FeeReportDto.createFromModel(FEE_REPORT),
            FlowReportDto.createFromModel(FLOW_REPORT),
            RebalanceReportDto.createFromModel(REBALANCE_REPORT),
            CHANNEL_WARNINGS.descriptions());
}
