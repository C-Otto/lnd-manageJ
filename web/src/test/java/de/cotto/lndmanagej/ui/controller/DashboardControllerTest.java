package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void dashboard() {
        StatusModel statusModel = new StatusModel(true, 213, NONE);
        when(pageService.dashboard()).thenReturn(new DashboardPage(List.of(), List.of(), statusModel));
        assertThat(dashboardController.dashboard(model)).isEqualTo("dashboard");
    }

    @Test
    void channels() {
        when(pageService.channels()).thenReturn(new ChannelsPage(List.of(OPEN_CHANNEL_DTO)));
        assertThat(dashboardController.channels(model)).isEqualTo("channels");
    }

    @Test
    void nodes() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE_PEER.alias(), true);
        when(pageService.nodes()).thenReturn(new NodesPage(List.of(nodeDto)));
        assertThat(dashboardController.nodes(model)).isEqualTo("nodes");
    }
}
