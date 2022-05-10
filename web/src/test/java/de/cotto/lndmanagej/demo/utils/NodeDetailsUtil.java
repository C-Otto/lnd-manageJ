package de.cotto.lndmanagej.demo.utils;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeReportFixtures;
import de.cotto.lndmanagej.model.FlowReportFixtures;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineReportFixtures;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReportFixtures;
import de.cotto.lndmanagej.ui.dto.NodeDto;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;

public final class NodeDetailsUtil {

    private NodeDetailsUtil() {
        // util class
    }

    public static NodeDetailsDto createNodeDetails(NodeDto node) {
        OnlineReport onlineReport = node.online()
                ? OnlineReportFixtures.ONLINE_REPORT : OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
        return new NodeDetailsDto(
                Pubkey.create(node.pubkey()),
                node.alias(),
                List.of(ChannelId.fromCompactForm("712345x123x1")),
                List.of(ChannelId.fromCompactForm("712345x123x1")),
                List.of(),
                List.of(),
                OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
                OnlineReportDto.createFromModel(onlineReport),
                FeeReportDto.createFromModel(FeeReportFixtures.FEE_REPORT),
                FlowReportDto.createFromModel(FlowReportFixtures.FLOW_REPORT),
                RebalanceReportDto.createFromModel(RebalanceReportFixtures.REBALANCE_REPORT),
                Set.of("Something is wrong with this node."));
    }
}
