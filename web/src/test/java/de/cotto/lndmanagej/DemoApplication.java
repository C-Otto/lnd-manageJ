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
import de.cotto.lndmanagej.model.ChannelId;
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
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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

@Configuration
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan("de.cotto.lndmanagej.ui")
public class DemoApplication {

    public static void main(String[] arguments) {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.ERROR);
        SpringApplication.run(DemoApplication.class, arguments);
    }

    @Component
    public static class DataServiceMock extends UiDataService {

        @Override
        public StatusModel getStatus() {
            return new StatusModel(true, 735642, createNodeWarnings());
        }

        private NodesAndChannelsWithWarningsDto createNodeWarnings() {
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

        @Override
        public List<OpenChannelDto> getOpenChannels() {
            return List.of(OPEN_CHANNEL_DTO, ACINQ, ACINQ2, WOS, WOS2, BCASH, COTTO);
        }

        @Override
        public ChanDetailsDto getChannelDetails(ChannelId channelId) {
            OpenChannelDto localOpenChannel = getOpenChannels().stream()
                    .filter(c -> c.channelId().equals(channelId))
                    .findFirst()
                    .orElseThrow();
            return createChannelDetails(localOpenChannel);
        }

        private ChanDetailsDto createChannelDetails(OpenChannelDto channel) {
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

        @Override
        public NodeDto getNode(Pubkey pubkey) {
            return getOpenChannels().stream()
                    .filter(channel -> channel.remotePubkey().equals(pubkey))
                    .map(channel -> new NodeDto(pubkey.toString(), channel.remoteAlias(), isOnline(channel)))
                    .findFirst().orElseThrow();
        }

        @Override
        public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
            return createNodeDetails(getNode(pubkey));
        }

        private static NodeDetailsDto createNodeDetails(NodeDto node) {
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

    private static boolean isOnline(OpenChannelDto c) {
        return c.channelId().getShortChannelId() % 2 != 0;
    }
}