package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelController;
import de.cotto.lndmanagej.controller.NodeController;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.StatusController;
import de.cotto.lndmanagej.controller.WarningsController;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.controller.dto.ChannelStatusDtoFixture.CHANNEL_STATUS_PRIVATE_OPEN;
import static de.cotto.lndmanagej.controller.dto.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_NUM_UPDATES_WARNING;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.CAPACITY_SAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UiDataServiceImplTest {
    @InjectMocks
    private UiDataServiceImpl uiDataService;

    @Mock
    private StatusController statusController;

    @Mock
    private WarningsController warningsController;

    @Mock
    private ChannelController channelController;

    @Mock
    private ChannelService channelService;

    @Mock
    private NodeController nodeController;

    @Mock
    private NodeService nodeService;

    @Test
    void getWarnings() {
        NodesAndChannelsWithWarningsDto warnings = new NodesAndChannelsWithWarningsDto(List.of(), List.of());
        when(warningsController.getWarnings()).thenReturn(warnings);
        assertThat(uiDataService.getWarnings()).isEqualTo(warnings);
    }

    @Test
    void getOpenChannels() {
        String alias = "remote alias";
        PoliciesDto policies = PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL);
        BalanceInformationDto balance = BalanceInformationDto.createFromModel(BALANCE_INFORMATION);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(nodeController.getAlias(PUBKEY_2)).thenReturn(alias);
        when(channelController.getPolicies(CHANNEL_ID)).thenReturn(policies);
        when(channelController.getBalance(CHANNEL_ID)).thenReturn(balance);
        when(statusController.getOpenChannels()).thenReturn(new ChannelsDto(List.of(CHANNEL_ID)));

        assertThat(uiDataService.getOpenChannels()).containsExactly(
                new OpenChannelDto(CHANNEL_ID, alias, PUBKEY_2, policies, BALANCE_INFORMATION_MODEL, CAPACITY_SAT)
        );
    }

    @Test
    void getChannelDetails() throws Exception {
        when(channelController.getDetails(CHANNEL_ID)).thenReturn(ChannelDetailsDto.createFromModel(CHANNEL_DETAILS));
        assertThat(uiDataService.getChannelDetails(CHANNEL_ID)).isEqualTo(
                new de.cotto.lndmanagej.ui.dto.ChannelDetailsDto(
                        CHANNEL_ID,
                        PUBKEY_2,
                        ALIAS,
                        CHANNEL_STATUS_PRIVATE_OPEN,
                        LOCAL,
                        BALANCE_INFORMATION_MODEL,
                        CAPACITY_SAT,
                        OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                        PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
                        FeeReportDto.createFromModel(FEE_REPORT),
                        FlowReportDto.createFromModel(FLOW_REPORT),
                        RebalanceReportDto.createFromModel(REBALANCE_REPORT),
                        Set.of(CHANNEL_NUM_UPDATES_WARNING.description())
                ));
    }

    @Test
    void getChannelDetails_not_found() throws Exception {
        when(channelController.getDetails(CHANNEL_ID)).thenThrow(NotFoundException.class);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> uiDataService.getChannelDetails(CHANNEL_ID)
        );
    }

    @Test
    void getNode() {
        when(nodeService.getNode(PUBKEY)).thenReturn(NODE_PEER);
        assertThat(uiDataService.getNode(PUBKEY)).isEqualTo(
                new NodeDto(PUBKEY.toString(), NODE_PEER.alias(), true)
        );
    }

    @Test
    void getNodeDetails() {
        when(nodeController.getDetails(PUBKEY)).thenReturn(NODE_DETAILS_DTO);
        assertThat(uiDataService.getNodeDetails(PUBKEY)).isEqualTo(NODE_DETAILS_MODEL);
    }
}
