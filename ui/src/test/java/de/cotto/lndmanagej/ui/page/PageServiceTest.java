package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {
    @InjectMocks
    private PageService pageService;

    @Mock
    private UiDataService dataService;

    @Test
    void dashboard() {
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        List<NodeDto> nodes = List.of(new NodeDto(PUBKEY.toString(), NODE.alias(), true));
        mockChannelsAndNodesWithoutWarning(channels, nodes);

        assertThat(pageService.dashboard()).usingRecursiveComparison().isEqualTo(
                new DashboardPage(channels, nodes, NodesAndChannelsWithWarningsDto.NONE)
        );
    }

    @Test
    void dashboard_nodes_alphabeticalOrder() {
        NodeDto bob = new NodeDto(PUBKEY.toString(), "Bob", true);
        NodeDto alice = new NodeDto(PUBKEY_3.toString(), "Alice", true);
        NodeDto charlie = new NodeDto(PUBKEY_2.toString(), "Charlie", true);
        List<NodeDto> nodesUnsorted = List.of(bob, charlie, alice);
        mockChannelsAndNodesWithoutWarning(List.of(), nodesUnsorted);

        List<NodeDto> nodesSorted = List.of(alice, bob, charlie);
        assertThat(pageService.dashboard().getNodes()).isEqualTo(nodesSorted);
    }

    @Test
    void dashboard_nodes_grouped_offline_first() {
        NodeDto offlineNode1 = new NodeDto(PUBKEY_3.toString(), "Offline-Node1", false);
        NodeDto onlineNode = new NodeDto(PUBKEY.toString(), "Online-Node", true);
        NodeDto offlineNode2 = new NodeDto(PUBKEY_2.toString(), "Offline-Node2", false);
        List<NodeDto> nodesUnsorted = List.of(onlineNode, offlineNode2, offlineNode1);
        mockChannelsAndNodesWithoutWarning(List.of(), nodesUnsorted);

        List<NodeDto> nodesSorted = List.of(offlineNode1, offlineNode2, onlineNode);
        assertThat(pageService.dashboard().getNodes()).isEqualTo(nodesSorted);
    }

    @Test
    void dashboard_with_sort_key() {
        mockChannelsAndNodesWithoutWarning(List.of(), List.of());
        pageService.dashboard("bar");
        verify(dataService).getOpenChannels("bar");
    }

    private void mockChannelsAndNodesWithoutWarning(List<OpenChannelDto> channels, List<NodeDto> nodes) {
        when(dataService.getOpenChannels(any())).thenReturn(channels);
        when(dataService.createNodeList()).thenReturn(nodes);
        when(dataService.getWarnings()).thenReturn(NodesAndChannelsWithWarningsDto.NONE);
    }

    @Test
    void channels() {
        when(dataService.getOpenChannels(null)).thenReturn(List.of(OPEN_CHANNEL_DTO));
        assertThat(pageService.channels()).usingRecursiveComparison().isEqualTo(
                new ChannelsPage(List.of(OPEN_CHANNEL_DTO))
        );
    }

    @Test
    void channels_with_sort_key() {
        when(dataService.getOpenChannels(any())).thenReturn(List.of());
        pageService.channels("foo");
        verify(dataService).getOpenChannels("foo");
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
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
        when(dataService.createNodeList()).thenReturn(List.of(nodeDto));

        assertThat(pageService.nodes()).usingRecursiveComparison().isEqualTo(
                new NodesPage(List.of(nodeDto))
        );
    }

    @Test
    void nodes_sorted() {
        NodeDto bob = new NodeDto(PUBKEY.toString(), "Bob", true);
        NodeDto alice = new NodeDto(PUBKEY_3.toString(), "alice", true);
        NodeDto charlie = new NodeDto(PUBKEY_2.toString(), "Charlie", false);
        List<NodeDto> nodesUnsorted = List.of(bob, charlie, alice);
        when(dataService.createNodeList()).thenReturn(nodesUnsorted);

        List<NodeDto> nodesSorted = List.of(charlie, alice, bob);
        assertThat(pageService.nodes().getNodes()).isEqualTo(nodesSorted);
    }

    @Test
    void nodes_for_channels() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        when(dataService.createNodeList(Set.of(PUBKEY))).thenReturn(List.of(nodeDto));

        assertThat(pageService.nodes(channels)).usingRecursiveComparison().isEqualTo(
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
}
