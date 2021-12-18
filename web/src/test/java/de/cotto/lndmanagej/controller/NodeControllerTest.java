package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeDetailsService;
import de.cotto.lndmanagej.service.NodeService;
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
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeControllerTest {
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @InjectMocks
    private NodeController nodeController;

    @Mock
    private NodeService nodeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private FeeService feeService;

    @Mock
    private NodeDetailsService nodeDetailsService;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);

        assertThat(nodeController.getAlias(PUBKEY_2)).isEqualTo(ALIAS_2);
    }

    @Test
    void getNodeDetails() {
        when(nodeDetailsService.getDetails(PUBKEY_2)).thenReturn(NODE_DETAILS);
        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(NodeDetailsDto.createFromModel(NODE_DETAILS));
    }

    @Test
    void getOpenChannelIds() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        assertThat(nodeController.getOpenChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_3)));
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
    }

    @Test
    void getAllChannelIds_ordered() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2, LOCAL_OPEN_CHANNEL));
        assertThat(nodeController.getAllChannelIdsForPubkey(PUBKEY))
                .isEqualTo(new ChannelsForNodeDto(PUBKEY, List.of(CHANNEL_ID, CHANNEL_ID_2)));
    }

    @Test
    void getBalance() {
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BALANCE_INFORMATION);
        assertThat(nodeController.getBalance(PUBKEY))
                .isEqualTo(BalanceInformationDto.createFromModel(BALANCE_INFORMATION));
    }

    @Test
    void getFeeReport() {
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FEE_REPORT);
        assertThat(nodeController.getFeeReport(PUBKEY)).isEqualTo(FeeReportDto.createFromModel(FEE_REPORT));
    }
}