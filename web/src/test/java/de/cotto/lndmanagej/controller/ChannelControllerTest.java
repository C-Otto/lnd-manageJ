package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.ChannelDto;
import de.cotto.lndmanagej.controller.dto.ClosedChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OffChainCostService;
import de.cotto.lndmanagej.service.OnChainCostService;
import de.cotto.lndmanagej.service.PolicyService;
import de.cotto.lndmanagej.service.RebalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.OffChainCostsFixtures.OFF_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {
    private static final PoliciesDto FEE_CONFIGURATION_DTO = PoliciesDto.createFromModel(POLICIES);
    private static final ClosedChannelDetailsDto CLOSED_CHANNEL_DETAILS_DTO =
            ClosedChannelDetailsDto.createFromModel(CLOSED_CHANNEL);
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @InjectMocks
    private ChannelController channelController;

    @Mock
    private ChannelService channelService;

    @Mock
    private NodeService nodeService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private OffChainCostService offChainCostService;

    @Mock
    private PolicyService policyService;

    @Mock
    private FeeService feeService;

    @Mock
    private RebalanceService rebalanceService;

    @BeforeEach
    void setUp() {
        lenient().when(onChainCostService.getOnChainCostsForChannelId(CHANNEL_ID)).thenReturn(ON_CHAIN_COSTS);
        lenient().when(offChainCostService.getOffChainCostsForChannel(CHANNEL_ID)).thenReturn(OFF_CHAIN_COSTS);
        lenient().when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
        lenient().when(feeService.getFeeReportForChannel(CHANNEL_ID)).thenReturn(FEE_REPORT);
        lenient().when(rebalanceService.getRebalanceAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.NONE);
        lenient().when(rebalanceService.getRebalanceAmountToChannel(CHANNEL_ID)).thenReturn(Coins.NONE);
    }

    @Test
    void getBasicInformation_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelController.getBasicInformation(CHANNEL_ID));
    }

    @Test
    void getBasicInformation() throws NotFoundException {
        ChannelDto basicInformation = new ChannelDto(LOCAL_OPEN_CHANNEL, ClosedChannelDetailsDto.UNKNOWN);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(channelController.getBasicInformation(CHANNEL_ID)).isEqualTo(basicInformation);
    }

    @Test
    void getBasicInformation_closed_channel() throws NotFoundException {
        ChannelDto basicInformation = new ChannelDto(CLOSED_CHANNEL, CLOSED_CHANNEL_DETAILS_DTO);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(channelController.getBasicInformation(CHANNEL_ID)).isEqualTo(basicInformation);
    }

    @Test
    void getDetails_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelController.getDetails(CHANNEL_ID));
    }

    @Test
    void getDetails() throws NotFoundException {
        ChannelDetailsDto expectedDetails = new ChannelDetailsDto(
                LOCAL_OPEN_CHANNEL,
                ALIAS_2,
                LOCAL_OPEN_CHANNEL.getBalanceInformation(),
                ON_CHAIN_COSTS,
                OFF_CHAIN_COSTS,
                FEE_CONFIGURATION_DTO,
                ClosedChannelDetailsDto.UNKNOWN,
                FEE_REPORT,
                Coins.ofMilliSatoshis(1),
                Coins.ofMilliSatoshis(2)
        );
        when(rebalanceService.getRebalanceAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(1));
        when(rebalanceService.getRebalanceAmountToChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(2));
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getBalanceInformation(CHANNEL_ID))
                .thenReturn(Optional.ofNullable(LOCAL_OPEN_CHANNEL.getBalanceInformation()));

        assertThat(channelController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_private() throws NotFoundException {
        ChannelDetailsDto expectedDetails = new ChannelDetailsDto(
                LOCAL_OPEN_CHANNEL_PRIVATE,
                ALIAS_2,
                LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation(),
                ON_CHAIN_COSTS,
                OFF_CHAIN_COSTS,
                FEE_CONFIGURATION_DTO,
                ClosedChannelDetailsDto.UNKNOWN,
                FEE_REPORT,
                Coins.NONE,
                Coins.NONE
        );
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
        when(balanceService.getBalanceInformation(CHANNEL_ID))
                .thenReturn(Optional.ofNullable(LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation()));

        assertThat(channelController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_closed() throws NotFoundException {
        ChannelDetailsDto expectedDetails = mockForChannelWithoutPolicies(CLOSED_CHANNEL);
        assertThat(channelController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_waiting_close() throws NotFoundException {
        ChannelDetailsDto expectedDetails = mockForChannelWithoutPolicies(WAITING_CLOSE_CHANNEL);
        assertThat(channelController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getBalance() {
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.of(BALANCE_INFORMATION));
        assertThat(channelController.getBalance(CHANNEL_ID))
                .isEqualTo(BalanceInformationDto.createFromModel(BALANCE_INFORMATION));
    }

    @Test
    void getBalance_not_found() {
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(channelController.getBalance(CHANNEL_ID))
                .isEqualTo(BalanceInformationDto.createFromModel(BalanceInformation.EMPTY));
    }

    @Test
    void getPolicies() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
        assertThat(channelController.getPolicies(CHANNEL_ID)).isEqualTo(FEE_CONFIGURATION_DTO);
    }

    @Test
    void getPolicies_waiting_close() {
        assertThat(channelController.getPolicies(CHANNEL_ID)).isEqualTo(PoliciesDto.EMPTY);
    }

    @Test
    void getCloseDetails() throws NotFoundException {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(channelController.getCloseDetails(CHANNEL_ID)).isEqualTo(CLOSED_CHANNEL_DETAILS_DTO);
    }

    @Test
    void getCloseDetails_not_closed() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelController.getCloseDetails(CHANNEL_ID));
    }

    @Test
    void getFeeReport() {
        when(feeService.getFeeReportForChannel(CHANNEL_ID)).thenReturn(FEE_REPORT);
        assertThat(channelController.getFeeReport(CHANNEL_ID)).isEqualTo(FeeReportDto.createFromModel(FEE_REPORT));
    }

    private ChannelDetailsDto mockForChannelWithoutPolicies(LocalChannel channel) {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.empty());
        return new ChannelDetailsDto(
                channel,
                ALIAS_2,
                BalanceInformation.EMPTY,
                ON_CHAIN_COSTS,
                OFF_CHAIN_COSTS,
                PoliciesDto.EMPTY,
                ClosedChannelDetailsDto.createFromModel(channel),
                FEE_REPORT,
                Coins.NONE,
                Coins.NONE
        );
    }
}