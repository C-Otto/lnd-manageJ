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
import de.cotto.lndmanagej.controller.dto.PolicyDto;
import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import de.cotto.lndmanagej.service.RatingService;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_NUM_UPDATES_WARNING;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.CAPACITY_SAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private RatingService ratingService;

    @Test
    void getWarnings() {
        NodesAndChannelsWithWarningsDto warnings = new NodesAndChannelsWithWarningsDto(List.of(), List.of());
        when(warningsController.getWarnings()).thenReturn(warnings);
        assertThat(uiDataService.getWarnings()).isEqualTo(warnings);
    }

    @Test
    void getPubkeys() {
        when(channelService.getOpenChannels())
                .thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_PRIVATE, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        assertThat(uiDataService.getPubkeys()).isEqualTo(Set.of(PUBKEY_2, PUBKEY_3));
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
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(RATING));

        assertThat(uiDataService.getOpenChannels()).containsExactly(
                new OpenChannelDto(
                        CHANNEL_ID,
                        alias,
                        PUBKEY_2,
                        policies,
                        BALANCE_INFORMATION_MODEL,
                        CAPACITY_SAT,
                        false,
                        RATING.getRating())
        );
    }

    @Nested
    class Sorted {

        private static final PolicyDto ZERO_POLICY = policy(0, 0);

        @BeforeEach
        void setup() {
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            when(channelService.getLocalChannel(CHANNEL_ID_2)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
            when(channelService.getLocalChannel(CHANNEL_ID_3)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_3));
            when(channelController.getBalance(any()))
                    .thenReturn(BalanceInformationDto.createFromModel(BalanceInformation.EMPTY));
            when(statusController.getOpenChannels()).thenReturn(new ChannelsDto(
                    List.of(CHANNEL_ID_3, CHANNEL_ID, CHANNEL_ID_2)
            ));
        }

        @Test
        void default_by_ratio() {
            when(channelController.getBalance(CHANNEL_ID)).thenReturn(balanceWithLocalSat(200));
            when(channelController.getBalance(CHANNEL_ID_2)).thenReturn(balanceWithLocalSat(400));
            when(channelController.getBalance(CHANNEL_ID_3)).thenReturn(balanceWithLocalSat(100));
            assertThat(uiDataService.getOpenChannels().stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_3, CHANNEL_ID, CHANNEL_ID_2);
        }

        @Test
        void by_announced() {
            when(channelService.getLocalChannel(CHANNEL_ID_3)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
            assertThat(uiDataService.getOpenChannels("announced").stream().map(OpenChannelDto::privateChannel))
                    .containsExactly(false, false, true);
        }

        @Test
        void by_inbound() {
            when(channelController.getBalance(CHANNEL_ID)).thenReturn(balanceWithRemoteSat(1));
            when(channelController.getBalance(CHANNEL_ID_2)).thenReturn(balanceWithRemoteSat(400));
            when(channelController.getBalance(CHANNEL_ID_3)).thenReturn(balanceWithRemoteSat(100));
            assertThat(uiDataService.getOpenChannels("inbound").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_ratio() {
            when(channelController.getBalance(CHANNEL_ID)).thenReturn(balanceWithLocalSat(10));
            when(channelController.getBalance(CHANNEL_ID_2)).thenReturn(balanceWithLocalSat(40));
            when(channelController.getBalance(CHANNEL_ID_3)).thenReturn(balanceWithLocalSat(20));
            assertThat(uiDataService.getOpenChannels("ratio").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_outbound() {
            when(channelController.getBalance(CHANNEL_ID)).thenReturn(balanceWithLocalSat(2));
            when(channelController.getBalance(CHANNEL_ID_2)).thenReturn(balanceWithLocalSat(99));
            when(channelController.getBalance(CHANNEL_ID_3)).thenReturn(balanceWithLocalSat(10));
            assertThat(uiDataService.getOpenChannels("outbound").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_capacity() {
            when(channelService.getLocalChannel(CHANNEL_ID_3)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_TO_NODE_3));
            assertThat(uiDataService.getOpenChannels("capacity").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3);
        }

        @Test
        void by_local_base_fee() {
            when(channelController.getPolicies(CHANNEL_ID)).thenReturn(new PoliciesDto(policy(0, 0), ZERO_POLICY));
            when(channelController.getPolicies(CHANNEL_ID_2)).thenReturn(new PoliciesDto(policy(0, 2), ZERO_POLICY));
            when(channelController.getPolicies(CHANNEL_ID_3)).thenReturn(new PoliciesDto(policy(0, 1), ZERO_POLICY));
            assertThat(uiDataService.getOpenChannels("localbasefee").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_remote_base_fee() {
            when(channelController.getPolicies(CHANNEL_ID)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(0, 0)));
            when(channelController.getPolicies(CHANNEL_ID_2)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(0, 2)));
            when(channelController.getPolicies(CHANNEL_ID_3)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(0, 1)));
            assertThat(uiDataService.getOpenChannels("remotebasefee").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_local_fee_rate() {
            when(channelController.getPolicies(CHANNEL_ID)).thenReturn(new PoliciesDto(policy(5, 0), ZERO_POLICY));
            when(channelController.getPolicies(CHANNEL_ID_2)).thenReturn(new PoliciesDto(policy(2, 2), ZERO_POLICY));
            when(channelController.getPolicies(CHANNEL_ID_3)).thenReturn(new PoliciesDto(policy(3, 1), ZERO_POLICY));
            assertThat(uiDataService.getOpenChannels("localfeerate").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID_2, CHANNEL_ID_3, CHANNEL_ID);
        }

        @Test
        void by_remote_fee_rate() {
            when(channelController.getPolicies(CHANNEL_ID)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(1, 0)));
            when(channelController.getPolicies(CHANNEL_ID_2)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(5, 2)));
            when(channelController.getPolicies(CHANNEL_ID_3)).thenReturn(new PoliciesDto(ZERO_POLICY, policy(3, 1)));
            assertThat(uiDataService.getOpenChannels("remotefeerate").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_2);
        }

        @Test
        void by_alias() {
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_TO_NODE_3));
            when(nodeController.getAlias(LOCAL_OPEN_CHANNEL.getRemotePubkey())).thenReturn("z");
            when(nodeController.getAlias(LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey())).thenReturn("a");
            assertThat(uiDataService.getOpenChannels("alias").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3);
        }

        private static PolicyDto policy(int feeRate, int baseFee) {
            return new PolicyDto(feeRate, String.valueOf(baseFee), true, 0, "0");
        }

        @Test
        void getOpenChannels_sorted_by_channel_id() {
            when(channelController.getBalance(any()))
                    .thenReturn(BalanceInformationDto.createFromModel(BalanceInformation.EMPTY));
            when(statusController.getOpenChannels())
                    .thenReturn(new ChannelsDto(List.of(CHANNEL_ID_3, CHANNEL_ID, CHANNEL_ID_2)));

            assertThat(uiDataService.getOpenChannels("channelid").stream().map(OpenChannelDto::channelId))
                    .containsExactly(CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_3);
        }

        private BalanceInformationDto balanceWithRemoteSat(int satoshis) {
            BalanceInformation balanceInformation =
                    new BalanceInformation(Coins.ofSatoshis(1), Coins.NONE, Coins.ofSatoshis(satoshis), Coins.NONE);
            return BalanceInformationDto.createFromModel(balanceInformation);
        }

        private BalanceInformationDto balanceWithLocalSat(int satoshis) {
            BalanceInformation balanceInformation =
                    new BalanceInformation(Coins.ofSatoshis(satoshis), Coins.NONE, Coins.ofSatoshis(1), Coins.NONE);
            return BalanceInformationDto.createFromModel(balanceInformation);
        }
    }

    @Test
    void getChannelDetails() throws Exception {
        ChannelDetailsDto channelDetailsDto = ChannelDetailsDto.createFromModel(CHANNEL_DETAILS);
        when(channelController.getDetails(CHANNEL_ID)).thenReturn(channelDetailsDto);
        when(ownNodeService.getBlockHeight()).thenReturn(764_905);
        assertThat(uiDataService.getChannelDetails(CHANNEL_ID)).isEqualTo(
                new de.cotto.lndmanagej.ui.dto.ChannelDetailsDto(
                        CHANNEL_ID,
                        PUBKEY_2,
                        ALIAS,
                        365,
                        CHANNEL_STATUS_PRIVATE_OPEN,
                        LOCAL,
                        BALANCE_INFORMATION_MODEL,
                        CAPACITY_SAT,
                        OnChainCostsDto.createFromModel(ON_CHAIN_COSTS),
                        PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
                        FeeReportDto.createFromModel(FEE_REPORT),
                        FlowReportDto.createFromModel(FLOW_REPORT),
                        RebalanceReportDto.createFromModel(REBALANCE_REPORT),
                        Set.of(CHANNEL_NUM_UPDATES_WARNING.description()),
                        RatingDto.fromModel(RATING)));
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
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(RATING);
        assertThat(uiDataService.getNode(PUBKEY)).isEqualTo(
                new NodeDto(PUBKEY.toString(), NODE_PEER.alias(), true, RATING.getRating())
        );
    }

    @Test
    void getNodeDetails() {
        when(nodeController.getDetails(PUBKEY)).thenReturn(NODE_DETAILS_DTO);
        when(channelService.getClosedChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL));
        assertThat(uiDataService.getNodeDetails(PUBKEY)).isEqualTo(NODE_DETAILS_MODEL);
    }
}
