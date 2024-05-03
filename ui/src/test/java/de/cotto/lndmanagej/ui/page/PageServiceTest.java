package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.PolicyDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.WarningService;
import de.cotto.lndmanagej.ui.controller.param.SortBy;
import de.cotto.lndmanagej.ui.dto.BalanceInformationModel;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.PendingOpenChannelDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.channel.PendingChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformation.EMPTY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelRatingFixtures.RATING;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PoliciesForLocalChannel.UNKNOWN;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO_2;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO_3;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixtures.CAPACITY_SAT;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixtures.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixtures.OPEN_CHANNEL_DTO2;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixtures.UNANNOUNCED_CHANNEL;
import static de.cotto.lndmanagej.ui.dto.PendingOpenChannelDtoFixture.PENDING_OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.dto.warning.DashboardWarningsFixture.DASHBOARD_WARNING;
import static de.cotto.lndmanagej.ui.dto.warning.DashboardWarningsFixture.DASHBOARD_WARNING_2;
import static de.cotto.lndmanagej.ui.dto.warning.DashboardWarningsFixture.DASHBOARD_WARNING_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {
    @InjectMocks
    private PageService pageService;

    @Mock
    private UiDataService dataService;

    @Mock
    private WarningService warningService;

    @Test
    void dashboard_noWarnings() {
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        List<NodeDto> nodes = List.of(NODE_DTO);
        mockChannelsAndNodesAndWarnings(channels, nodes, List.of());

        assertThat(pageService.dashboard(SortBy.DEFAULT_SORT)).usingRecursiveComparison().isEqualTo(
                new DashboardPage(channels, nodes, List.of())
        );
    }

    @Test
    void dashboard_withWarnings_sorted() {
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        List<NodeDto> nodes = List.of(NODE_DTO);
        List<DashboardWarningDto> warnings = List.of(DASHBOARD_WARNING, DASHBOARD_WARNING_2, DASHBOARD_WARNING_3);
        mockChannelsAndNodesAndWarnings(channels, nodes, warnings);

        List<DashboardWarningDto> sortedWarnings = List.of(DASHBOARD_WARNING_2, DASHBOARD_WARNING, DASHBOARD_WARNING_3);
        assertThat(pageService.dashboard(SortBy.DEFAULT_SORT)).usingRecursiveComparison().isEqualTo(
                new DashboardPage(channels, nodes, sortedWarnings)
        );
    }

    @Test
    void dashboard_nodes_alphabeticalOrder() {
        NodeDto bob = new NodeDto(PUBKEY.toString(), "Bob", true, RATING.getValue());
        NodeDto alice = new NodeDto(PUBKEY_3.toString(), "Alice", true, RATING.getValue());
        NodeDto charlie = new NodeDto(PUBKEY_2.toString(), "Charlie", true, RATING.getValue());
        List<NodeDto> nodesUnsorted = List.of(bob, charlie, alice);
        mockChannelsAndNodesAndWarnings(List.of(), nodesUnsorted, List.of());

        List<NodeDto> nodesSorted = List.of(alice, bob, charlie);
        assertThat(pageService.dashboard(SortBy.DEFAULT_SORT).getNodes()).isEqualTo(nodesSorted);
    }

    @Test
    void dashboard_nodes_grouped_offline_first() {
        NodeDto offlineNode1 = new NodeDto(PUBKEY_3.toString(), "Offline-Node1", false, RATING.getValue());
        NodeDto onlineNode = new NodeDto(PUBKEY.toString(), "Online-Node", true, RATING.getValue());
        NodeDto offlineNode2 = new NodeDto(PUBKEY_2.toString(), "Offline-Node2", false, RATING.getValue());
        List<NodeDto> nodesUnsorted = List.of(onlineNode, offlineNode2, offlineNode1);
        mockChannelsAndNodesAndWarnings(List.of(), nodesUnsorted, List.of());

        List<NodeDto> nodesSorted = List.of(offlineNode1, offlineNode2, onlineNode);
        assertThat(pageService.dashboard(SortBy.DEFAULT_SORT).getNodes()).isEqualTo(nodesSorted);
    }

    private void mockChannelsAndNodesAndWarnings(
            List<OpenChannelDto> channels,
            List<NodeDto> nodes,
            List<DashboardWarningDto> warnings
    ) {
        when(dataService.getOpenChannels()).thenReturn(channels);
        when(dataService.createNodeList()).thenReturn(nodes);
        when(warningService.getWarnings()).thenReturn(warnings);
    }

    @Test
    void channels() {
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));

        assertThat(pageService.channels(SortBy.DEFAULT_SORT)).usingRecursiveComparison().isEqualTo(
                new ChannelsPage(List.of(OPEN_CHANNEL_DTO))
        );
    }

    @Test
    void channelDetails() throws NotFoundException {
        ChannelDetailsDto details = CHANNEL_DETAILS_DTO;
        when(dataService.getChannelDetails(CHANNEL_ID)).thenReturn(details);

        assertThat(pageService.channelDetails(CHANNEL_ID)).usingRecursiveComparison().isEqualTo(
                new ChannelDetailsPage(details)
        );
    }

    @Test
    void nodes() {
        when(dataService.createNodeList()).thenReturn(List.of(NODE_DTO));

        assertThat(pageService.nodes(SortBy.DEFAULT_SORT))
                .usingRecursiveComparison()
                .isEqualTo(new NodesPage(List.of(NODE_DTO)));
    }

    @Test
    void nodes_sorted() {
        NodeDto bob = new NodeDto(PUBKEY.toString(), "Bob", true, RATING.getValue());
        NodeDto alice = new NodeDto(PUBKEY_3.toString(), "alice", true, RATING.getValue());
        NodeDto charlie = new NodeDto(PUBKEY_2.toString(), "Charlie", false, RATING.getValue());
        List<NodeDto> nodesUnsorted = List.of(bob, charlie, alice);
        when(dataService.createNodeList()).thenReturn(nodesUnsorted);

        List<NodeDto> nodesSorted = List.of(charlie, alice, bob);
        assertThat(pageService.nodes(SortBy.DEFAULT_SORT).getNodes()).isEqualTo(nodesSorted);
    }

    @Test
    void nodes_for_channels() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE.alias(), true, RATING.getValue());
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        when(dataService.createNodeList(Set.of(PUBKEY))).thenReturn(List.of(nodeDto));

        assertThat(pageService.nodes(channels, SortBy.DEFAULT_SORT)).usingRecursiveComparison().isEqualTo(
                new NodesPage(List.of(nodeDto))
        );
    }

    @Test
    void nodeDetails() {
        when(dataService.getNodeDetails(PUBKEY)).thenReturn(NODE_DETAILS_MODEL);
        assertThat(pageService.nodeDetails(PUBKEY)).usingRecursiveComparison().isEqualTo(
                new NodeDetailsPage(NODE_DETAILS_MODEL)
        );
    }

    @Test
    void error() {
        String errorMessage = "foo";
        assertThat(pageService.error(errorMessage)).usingRecursiveComparison().isEqualTo(new ErrorPage(errorMessage));
    }

    @Test
    void pendingChannels() {
        List<PendingOpenChannelDto> channels = List.of(PENDING_OPEN_CHANNEL_DTO);
        when(dataService.getPendingOpenChannels()).thenReturn(channels);
        assertThat(pageService.pendingChannels()).usingRecursiveComparison()
                .isEqualTo(new PendingChannelsPage(channels));
        assertThat(pageService.pendingChannels().getView())
                .isEqualTo("pending-channels-page");
    }

    @Nested
    class Sorted {

        private static final PolicyDto ZERO_POLICY = policy(0, 0);

        @Test
        void default_by_ratio() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, balanceWithLocalSat(200)),
                    channel(CHANNEL_ID_2, balanceWithLocalSat(400)),
                    channel(CHANNEL_ID_3, balanceWithLocalSat(100))
            ));
            assertThat(pageService.dashboard(SortBy.DEFAULT_SORT).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_3, CHANNEL_ID, CHANNEL_ID_2);
        }

        @Test
        void by_ratio() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, balanceWithLocalSat(10)),
                    channel(CHANNEL_ID_2, balanceWithLocalSat(40)),
                    channel(CHANNEL_ID_3, balanceWithLocalSat(20))
            ));
            assertThat(pageService.dashboard(SortBy.RATIO).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_announced() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    OPEN_CHANNEL_DTO,
                    UNANNOUNCED_CHANNEL,
                    OPEN_CHANNEL_DTO2
            ));
            assertThat(pageService.dashboard(SortBy.ANNOUNCED).getChannels().stream()
                    .map(OpenChannelDto::privateChannel))
                    .containsExactly(false, false, true);
        }

        @Test
        void by_inbound() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, balanceWithRemoteSat(1)),
                    channel(CHANNEL_ID_2, balanceWithRemoteSat(400)),
                    channel(CHANNEL_ID_3, balanceWithRemoteSat(100))
            ));
            assertThat(pageService.dashboard(SortBy.INBOUND).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_outbound() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, balanceWithLocalSat(2)),
                    channel(CHANNEL_ID_2, balanceWithLocalSat(99)),
                    channel(CHANNEL_ID_3, balanceWithLocalSat(10))
            ));
            assertThat(pageService.dashboard(SortBy.OUTBOUND).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_capacity() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, 1_000_000),
                    channel(CHANNEL_ID_2, 3_000_000),
                    channel(CHANNEL_ID_3, 2_000_000)
            ));
            assertThat(pageService.dashboard(SortBy.CAPACITY).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_local_base_fee() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, new PoliciesDto(policy(0, 0), ZERO_POLICY)),
                    channel(CHANNEL_ID_2, new PoliciesDto(policy(0, 2), ZERO_POLICY)),
                    channel(CHANNEL_ID_3, new PoliciesDto(policy(0, 1), ZERO_POLICY))
            ));
            assertThat(pageService.dashboard(SortBy.LOCAL_BASE_FEE).getChannels().stream()
                    .map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_remote_base_fee() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, new PoliciesDto(ZERO_POLICY, policy(0, 0))),
                    channel(CHANNEL_ID_2, new PoliciesDto(ZERO_POLICY, policy(0, 2))),
                    channel(CHANNEL_ID_3, new PoliciesDto(ZERO_POLICY, policy(0, 1)))
            ));
            assertThat(pageService.dashboard(SortBy.REMOTE_BASE_FEE).getChannels().stream()
                    .map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_local_fee_rate() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, new PoliciesDto(policy(5, 0), ZERO_POLICY)),
                    channel(CHANNEL_ID_2, new PoliciesDto(policy(2, 0), ZERO_POLICY)),
                    channel(CHANNEL_ID_3, new PoliciesDto(policy(3, 0), ZERO_POLICY))
            ));
            assertThat(pageService.dashboard(SortBy.LOCAL_FEE_RATE).getChannels().stream()
                    .map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_2, CHANNEL_ID_3, CHANNEL_ID);
        }

        @Test
        void by_remote_fee_rate() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, new PoliciesDto(ZERO_POLICY, policy(1, 0))),
                    channel(CHANNEL_ID_2, new PoliciesDto(ZERO_POLICY, policy(5, 2))),
                    channel(CHANNEL_ID_3, new PoliciesDto(ZERO_POLICY, policy(3, 1)))
            ));
            assertThat(pageService.dashboard(SortBy.REMOTE_FEE_RATE).getChannels().stream()
                    .map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_alias() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, "carol"),
                    channel(CHANNEL_ID_2, "Bob"),
                    channel(CHANNEL_ID_3, "alice")
            ));
            assertThat(pageService.dashboard(SortBy.ALIAS).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_3, CHANNEL_ID_2, CHANNEL_ID);
        }

        @Test
        void by_rating() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channelWithRating(CHANNEL_ID, 2),
                    channelWithRating(CHANNEL_ID_2, 3),
                    channelWithRating(CHANNEL_ID_3, 1)
            ));
            assertThat(pageService.dashboard(SortBy.RATING).getChannels().stream()
                    .map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_3, CHANNEL_ID, CHANNEL_ID_2);
        }

        @Test
        void by_channel_id() {
            when(dataService.getOpenChannels()).thenReturn(List.of(
                    channel(CHANNEL_ID, balanceWithLocalSat(3)),
                    channel(CHANNEL_ID_2, balanceWithLocalSat(2)),
                    channel(CHANNEL_ID_3, balanceWithLocalSat(1))
            ));
            assertThat(pageService.dashboard(SortBy.CHANNEL_ID).getChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3);
        }

        private OpenChannelDto channel(ChannelId channelId, BalanceInformation balance) {
            return new OpenChannelDto(channelId, "mock-with-balance", PUBKEY,
                    PoliciesDto.createFromModel(UNKNOWN),
                    BalanceInformationModel.createFromModel(balance),
                    CAPACITY_SAT, false, RATING.getValue()
            );
        }

        private OpenChannelDto channel(ChannelId channelId, PoliciesDto policiesDto) {
            return new OpenChannelDto(channelId, "mock-with-policies", PUBKEY,
                    policiesDto,
                    BalanceInformationModel.createFromModel(EMPTY),
                    CAPACITY_SAT, false, RATING.getValue()
            );
        }

        private OpenChannelDto channel(ChannelId channelId, String alias) {
            return new OpenChannelDto(channelId, alias, PUBKEY,
                    PoliciesDto.createFromModel(UNKNOWN),
                    BalanceInformationModel.createFromModel(EMPTY),
                    CAPACITY_SAT, false, RATING.getValue()
            );
        }

        private OpenChannelDto channel(ChannelId channelId, long capacity) {
            return new OpenChannelDto(channelId, "mock-with-capacity", PUBKEY,
                    PoliciesDto.createFromModel(UNKNOWN),
                    BalanceInformationModel.createFromModel(EMPTY),
                    capacity, false, RATING.getValue()
            );
        }

        private OpenChannelDto channelWithRating(ChannelId channelId, long rating) {
            return new OpenChannelDto(channelId, "mock-with-rating", PUBKEY,
                    PoliciesDto.createFromModel(UNKNOWN),
                    BalanceInformationModel.createFromModel(EMPTY),
                    CAPACITY_SAT, false, rating
            );
        }

        private static PolicyDto policy(int feeRate, int baseFee) {
            return new PolicyDto(feeRate, String.valueOf(baseFee), 0, "0",true, 0, "0", "0");
        }

        private BalanceInformation balanceWithRemoteSat(int satoshis) {
            return new BalanceInformation(Coins.ofSatoshis(1), Coins.NONE, Coins.ofSatoshis(satoshis), Coins.NONE);
        }

        private BalanceInformation balanceWithLocalSat(int satoshis) {
            return new BalanceInformation(Coins.ofSatoshis(satoshis), Coins.NONE, Coins.ofSatoshis(1), Coins.NONE);
        }
    }

    @Nested
    class NodesSorted {

        @Test
        void by_nodeRating() {
            when(dataService.createNodeList()).thenReturn(List.of(
                    nodeWithRating(PUBKEY, 2),
                    nodeWithRating(PUBKEY_2, 3),
                    nodeWithRating(PUBKEY_3, 1)
            ));
            assertThat(pageService.dashboard(SortBy.NODE_RATING).getNodes().stream()
                    .map(NodeDto::pubkey))
                    .containsExactly(PUBKEY_3.toString(), PUBKEY.toString(), PUBKEY_2.toString());
        }

        @Test
        void by_nodeAlias() {
            when(dataService.createNodeList()).thenReturn(List.of(
                    node(PUBKEY, "A", false),
                    node(PUBKEY_2, "b", true),
                    node(PUBKEY_3, "C", false)
            ));
            assertThat(pageService.dashboard(SortBy.NODE_ALIAS).getNodes().stream()
                    .map(NodeDto::pubkey))
                    .containsExactly(PUBKEY.toString(), PUBKEY_2.toString(), PUBKEY_3.toString());
        }

        @Test
        void by_pubkey() {
            when(dataService.createNodeList()).thenReturn(List.of(NODE_DTO_3, NODE_DTO, NODE_DTO_2));
            assertThat(pageService.dashboard(SortBy.PUBKEY).getNodes().stream()
                    .map(NodeDto::pubkey))
                    .containsExactly(PUBKEY.toString(), PUBKEY_2.toString(), PUBKEY_3.toString());
        }

        private NodeDto nodeWithRating(Pubkey pubkey, long rating) {
            return new NodeDto(pubkey.toString(), "node-with-rating", true, rating);
        }

        private NodeDto node(Pubkey pubkey, String alias, boolean online) {
            return new NodeDto(pubkey.toString(), alias, online, RATING.getValue());
        }
    }
}
