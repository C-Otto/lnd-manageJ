package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeControllerTest {
    @InjectMocks
    private NodeController nodeController;

    @Mock
    private NodeService nodeService;

    @Mock
    private Metrics metrics;

    @Mock
    private ChannelService channelService;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private FeeService feeService;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);

        assertThat(nodeController.getAlias(PUBKEY_2)).isEqualTo(ALIAS_2);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAlias")));
    }

    @Test
    void getNodeDetails_no_channels() {
        when(onChainCostService.getOpenCostsWith(any())).thenReturn(Coins.NONE);
        when(onChainCostService.getCloseCostsWith(any())).thenReturn(Coins.NONE);
        when(balanceService.getBalanceInformation(any(Pubkey.class))).thenReturn(BalanceInformation.EMPTY);
        when(feeService.getEarnedFeesForPeer(any())).thenReturn(Coins.NONE);
        NodeDetailsDto expectedDetails = new NodeDetailsDto(
                PUBKEY_2,
                ALIAS_2,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new OnChainCostsDto(Coins.NONE, Coins.NONE),
                BalanceInformationDto.createFrom(BalanceInformation.EMPTY),
                true,
                new FeeReportDto("0")
        );
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, true));

        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(expectedDetails);
        verify(metrics).mark(argThat(name -> name.endsWith(".getDetails")));
    }

    @Test
    void getNodeDetails_with_channels() {
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, false));
        when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_3, LOCAL_OPEN_CHANNEL));
        when(channelService.getClosedChannelsWith(PUBKEY_2)).thenReturn(Set.of(CLOSED_CHANNEL_2, CLOSED_CHANNEL_3));
        when(channelService.getWaitingCloseChannelsFor(PUBKEY_2)).thenReturn(
                Set.of(WAITING_CLOSE_CHANNEL, WAITING_CLOSE_CHANNEL_2)
        );
        when(channelService.getForceClosingChannelsFor(PUBKEY_2)).thenReturn(
                Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_2, FORCE_CLOSING_CHANNEL_3)
        );
        Coins openCosts = Coins.ofSatoshis(123);
        Coins closeCosts = Coins.ofSatoshis(456);
        when(onChainCostService.getOpenCostsWith(PUBKEY_2)).thenReturn(openCosts);
        when(onChainCostService.getCloseCostsWith(PUBKEY_2)).thenReturn(closeCosts);
        when(balanceService.getBalanceInformation(PUBKEY_2)).thenReturn(BALANCE_INFORMATION);
        when(feeService.getEarnedFeesForPeer(any())).thenReturn(Coins.ofMilliSatoshis(1234));
        NodeDetailsDto expectedDetails = new NodeDetailsDto(
                PUBKEY_2,
                ALIAS_2,
                List.of(CHANNEL_ID, CHANNEL_ID_3),
                List.of(CHANNEL_ID_2, CHANNEL_ID_3),
                List.of(CHANNEL_ID, CHANNEL_ID_2),
                List.of(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3),
                new OnChainCostsDto(openCosts, closeCosts),
                BalanceInformationDto.createFrom(BALANCE_INFORMATION),
                false,
                new FeeReportDto("1234")
        );

        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(expectedDetails);
    }

    @Test
    void getOpenChannelIds() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(nodeController.getOpenChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_3)));
        verify(metrics).mark(argThat(name -> name.endsWith(".getOpenChannelIdsForPubkey")));
    }

    @Test
    void getOpenChannelIds_ordered() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_2, LOCAL_OPEN_CHANNEL));
        assertThat(nodeController.getOpenChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_2)));
    }

    @Test
    void getAllChannelIds() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        assertThat(nodeController.getAllChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_3)));
        verify(metrics).mark(argThat(name -> name.endsWith(".getAllChannelIdsForPubkey")));
    }

    @Test
    void getAllChannelIds_ordered() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2, LOCAL_OPEN_CHANNEL));
        assertThat(nodeController.getAllChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_2)));
    }

    @Test
    void getBalance() {
        when(balanceService.getBalanceInformation(PUBKEY)).thenReturn(BALANCE_INFORMATION);
        assertThat(nodeController.getBalance(PUBKEY)).isEqualTo(BalanceInformationDto.createFrom(BALANCE_INFORMATION));
        verify(metrics).mark(argThat(name -> name.endsWith(".getBalance")));
    }

    @Test
    void getFeeReport() {
        when(feeService.getEarnedFeesForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(1_234));
        assertThat(nodeController.getFeeReport(PUBKEY)).isEqualTo(new FeeReportDto("1234"));
        verify(metrics).mark(argThat(name -> name.endsWith(".getFeeReport")));
    }
}