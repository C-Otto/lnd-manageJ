package de.cotto.lndmanagej;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelIdFixtures;
import de.cotto.lndmanagej.model.FeeReportFixtures;
import de.cotto.lndmanagej.model.FlowReportFixtures;
import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineReportFixtures;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReportFixtures;
import de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures;
import de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures;
import de.cotto.lndmanagej.model.warnings.Warning;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;

import java.util.List;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ2;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.BCASH;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.COTTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS2;

public final class MockUtil {

    private MockUtil() {
        // util class
    }

    public static StatusModel getStatusModel() {
        return new StatusModel(true, 735_642, createNodeWarnings());
    }


    public static List<OpenChannelDto> createOpenChannels() {
        return List.of(OPEN_CHANNEL_DTO, ACINQ, ACINQ2, WOS, WOS2, BCASH, COTTO);
    }

    public static NodesAndChannelsWithWarningsDto createNodeWarnings() {
        return new NodesAndChannelsWithWarningsDto(
                List.of(new NodeWithWarningsDto(NodeWarningsFixtures.NODE_WARNINGS.warnings().stream()
                                .map(Warning::description)
                                .collect(Collectors.toSet()), WOS.remoteAlias(), WOS.remotePubkey()),
                        new NodeWithWarningsDto(NodeWarningsFixtures.NODE_WARNINGS.warnings().stream()
                                .map(Warning::description)
                                .collect(Collectors.toSet()), ACINQ.remoteAlias(), ACINQ.remotePubkey())
                ),
                List.of(new ChannelWithWarningsDto(ChannelWarningsFixtures.CHANNEL_WARNINGS.warnings().stream()
                        .map(Warning::description)
                        .collect(Collectors.toSet()), WOS.channelId())
                )
        );
    }

    public static ChanDetailsDto createChannelDetails(OpenChannelDto channel) {
        return new ChanDetailsDto(
                channel.channelId(),
                channel.remotePubkey(),
                channel.remoteAlias(),
                OpenInitiator.REMOTE,
                channel.balanceInformation(),
                OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                channel.policies(),
                FeeReportDto.createFromModel(FeeReportFixtures.FEE_REPORT),
                FlowReportDto.createFromModel(FlowReportFixtures.FLOW_REPORT),
                RebalanceReportDto.createFromModel(RebalanceReportFixtures.REBALANCE_REPORT),
                ChannelWarningsFixtures.CHANNEL_WARNINGS.descriptions());
    }

    public static NodeDetailsDto createNodeDetails(NodeDto node) {
        OnlineReport onlineReport = node.online()
                ? OnlineReportFixtures.ONLINE_REPORT : OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
        return new NodeDetailsDto(
                Pubkey.create(node.pubkey()),
                node.alias(),
                List.of(ChannelIdFixtures.CHANNEL_ID),
                List.of(ChannelIdFixtures.CHANNEL_ID_2),
                List.of(),
                List.of(),
                OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
                OnlineReportDto.createFromModel(onlineReport),
                FeeReportDto.createFromModel(FeeReportFixtures.FEE_REPORT),
                FlowReportDto.createFromModel(FlowReportFixtures.FLOW_REPORT),
                RebalanceReportDto.createFromModel(RebalanceReportFixtures.REBALANCE_REPORT),
                ChannelWarningsFixtures.CHANNEL_WARNINGS.descriptions());
    }
}
