package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.ChannelDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import de.cotto.lndmanagej.service.PolicyService;
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
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {
    private static final Coins OPEN_COSTS = Coins.ofSatoshis(1);
    private static final Coins CLOSE_COSTS = Coins.ofSatoshis(2);
    private static final OnChainCostsDto ON_CHAIN_COSTS = new OnChainCostsDto(OPEN_COSTS, CLOSE_COSTS);
    private static final PoliciesDto FEE_CONFIGURATION_DTO = PoliciesDto.createFrom(POLICIES);

    @InjectMocks
    private ChannelController channelController;

    @Mock
    private ChannelService channelService;

    @Mock
    private NodeService nodeService;

    @Mock
    private Metrics metrics;

    @Mock
    private BalanceService balanceService;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        lenient().when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(OPEN_COSTS));
        lenient().when(onChainCostService.getCloseCosts(CHANNEL_ID)).thenReturn(Optional.of(CLOSE_COSTS));
        lenient().when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
    }

    @Test
    void getBasicInformation_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelController.getBasicInformation(CHANNEL_ID));
    }

    @Test
    void getBasicInformation() throws NotFoundException {
        ChannelDto basicInformation = new ChannelDto(LOCAL_OPEN_CHANNEL);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(channelController.getBasicInformation(CHANNEL_ID)).isEqualTo(basicInformation);
        verify(metrics).mark(argThat(name -> name.endsWith(".getBasicInformation")));
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
                FEE_CONFIGURATION_DTO
        );
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getBalanceInformation(CHANNEL_ID))
                .thenReturn(Optional.ofNullable(LOCAL_OPEN_CHANNEL.getBalanceInformation()));

        assertThat(channelController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
        verify(metrics).mark(argThat(name -> name.endsWith(".getDetails")));
    }

    @Test
    void getDetails_private() throws NotFoundException {
        ChannelDetailsDto expectedDetails = new ChannelDetailsDto(
                LOCAL_OPEN_CHANNEL_PRIVATE,
                ALIAS_2,
                LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation(),
                ON_CHAIN_COSTS,
                FEE_CONFIGURATION_DTO
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
                .isEqualTo(BalanceInformationDto.createFrom(BALANCE_INFORMATION));
        verify(metrics).mark(argThat(name -> name.endsWith(".getBalance")));
    }

    @Test
    void getBalance_not_found() {
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(channelController.getBalance(CHANNEL_ID))
                .isEqualTo(BalanceInformationDto.createFrom(BalanceInformation.EMPTY));
        verify(metrics).mark(argThat(name -> name.endsWith(".getBalance")));
    }

    @Test
    void getPolicies() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
        assertThat(channelController.getPolicies(CHANNEL_ID)).isEqualTo(FEE_CONFIGURATION_DTO);
        verify(metrics).mark(argThat(name -> name.endsWith(".getPolicies")));
    }

    @Test
    void getPolicies_waiting_close() {
        assertThat(channelController.getPolicies(CHANNEL_ID)).isEqualTo(PoliciesDto.EMPTY);
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
                PoliciesDto.EMPTY
        );
    }
}