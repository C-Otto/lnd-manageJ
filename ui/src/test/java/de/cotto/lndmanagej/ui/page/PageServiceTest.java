package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.WarningsModel;
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

import static de.cotto.lndmanagej.controller.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.controller.dto.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
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
        WarningsModel warningsModel = new WarningsModel(new NodesAndChannelsWithWarningsDto(List.of(), List.of()));
        when(dataService.getOpenChannels()).thenReturn(channels);
        when(dataService.createNodeList()).thenReturn(nodes);
        when(dataService.getStatus()).thenReturn(warningsModel);

        assertThat(pageService.dashboard()).usingRecursiveComparison().isEqualTo(
                new DashboardPage(channels, nodes, warningsModel)
        );
    }

    @Test
    void channels() {
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        assertThat(pageService.channels()).usingRecursiveComparison().isEqualTo(
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
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
        when(dataService.createNodeList()).thenReturn(List.of(nodeDto));

        assertThat(pageService.nodes()).usingRecursiveComparison().isEqualTo(
                new NodesPage(List.of(nodeDto))
        );
    }

    @Test
    void nodes_for_channels() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        when(dataService.createNodeList(channels)).thenReturn(List.of(nodeDto));

        assertThat(pageService.nodes(channels)).usingRecursiveComparison().isEqualTo(
                new NodesPage(List.of(nodeDto))
        );
    }

    @Test
    void nodeDetails() {
        when(dataService.getNodeDetails(PUBKEY)).thenReturn(NODE_DETAILS_DTO);
        assertThat(pageService.nodeDetails(PUBKEY)).usingRecursiveComparison().isEqualTo(
                new NodeDetailsPage(NODE_DETAILS_DTO)
        );
    }

    @Test
    void error() {
        String errorMessage = "foo";
        assertThat(pageService.error(errorMessage)).usingRecursiveComparison().isEqualTo(new ErrorPage(errorMessage));
    }
}
