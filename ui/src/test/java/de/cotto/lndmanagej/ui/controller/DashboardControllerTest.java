package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
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
import java.util.Map;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.CAPACITY_SAT;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    private static final String CHANNELS_KEY = "channels";
    private static final String NODES_KEY = "nodes";

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void dashboard() {
        NodesAndChannelsWithWarningsDto warnings = NodesAndChannelsWithWarningsDto.NONE;
        when(pageService.dashboard()).thenReturn(new DashboardPage(List.of(), List.of(), warnings));
        assertThat(dashboardController.dashboard(model)).isEqualTo("dashboard");
        verify(model).addAllAttributes(
                Map.of(NODES_KEY, List.of(), CHANNELS_KEY, List.of(), "warnings", warnings)
        );
    }

    @Test
    void channels() {
        when(pageService.channels()).thenReturn(new ChannelsPage(List.of(OPEN_CHANNEL_DTO)));
        assertThat(dashboardController.channels(model)).isEqualTo(CHANNELS_KEY);
        verify(model).addAllAttributes(Map.of(CHANNELS_KEY, List.of(OPEN_CHANNEL_DTO)));
    }

    @Test
    void channels_sorted_by_outbound() {
        OpenChannelDto channelA = withBalance(Coins.ofSatoshis(2));
        OpenChannelDto channelB = withBalance(Coins.ofSatoshis(3));
        OpenChannelDto channelC = withBalance(Coins.ofSatoshis(1));
        when(pageService.channels()).thenReturn(new ChannelsPage(List.of(channelA, channelB, channelC)));
        assertThat(dashboardController.channels(model)).isEqualTo(CHANNELS_KEY);
        verify(model).addAllAttributes(Map.of(CHANNELS_KEY, List.of(channelC, channelA, channelB)));
    }

    @Test
    void nodes() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE_PEER.alias(), true);
        when(pageService.nodes()).thenReturn(new NodesPage(List.of(nodeDto)));
        assertThat(dashboardController.nodes(model)).isEqualTo(NODES_KEY);
        verify(model).addAllAttributes(Map.of(NODES_KEY, List.of(nodeDto)));
    }

    private OpenChannelDto withBalance(Coins localBalance) {
        BalanceInformation balanceInformation =
                new BalanceInformation(localBalance, Coins.NONE, REMOTE_BALANCE, Coins.NONE);
        return new OpenChannelDto(
                CHANNEL_ID,
                "Albert",
                PUBKEY,
                PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
                BalanceInformationDto.createFromModel(balanceInformation),
                CAPACITY_SAT
        );
    }
}
