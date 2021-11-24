package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.FeeConfigurationDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.FeeConfigurationFixtures.FEE_CONFIGURATION;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelDetailsControllerTest {
    private static final Coins OPEN_COSTS = Coins.ofSatoshis(1);
    private static final Coins CLOSE_COSTS = Coins.ofSatoshis(2);
    private static final OnChainCostsDto ON_CHAIN_COSTS = new OnChainCostsDto(OPEN_COSTS, CLOSE_COSTS);
    private static final FeeConfigurationDto FEE_CONFIGURATION_DTO = FeeConfigurationDto.createFrom(FEE_CONFIGURATION);

    @InjectMocks
    private ChannelDetailsController channelDetailsController;

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
    private FeeService feeService;

    @BeforeEach
    void setUp() {
        lenient().when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(OPEN_COSTS));
        lenient().when(onChainCostService.getCloseCosts(CHANNEL_ID)).thenReturn(Optional.of(CLOSE_COSTS));
        lenient().when(feeService.getFeeConfiguration(CHANNEL_ID)).thenReturn(FEE_CONFIGURATION);
    }

    @Test
    void getDetails_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelDetailsController.getDetails(CHANNEL_ID));
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

        assertThat(channelDetailsController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
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

        assertThat(channelDetailsController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_closed() throws NotFoundException {
        ChannelDetailsDto expectedDetails = mockForChannelWithoutFeeConfiguration(CLOSED_CHANNEL);
        assertThat(channelDetailsController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_waiting_close() throws NotFoundException {
        ChannelDetailsDto expectedDetails = mockForChannelWithoutFeeConfiguration(WAITING_CLOSE_CHANNEL);
        assertThat(channelDetailsController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
    }

    private ChannelDetailsDto mockForChannelWithoutFeeConfiguration(LocalChannel channel) {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.empty());
        return new ChannelDetailsDto(
                channel,
                ALIAS_2,
                BalanceInformation.EMPTY,
                ON_CHAIN_COSTS,
                new FeeConfigurationDto(0, 0, 0, 0)
        );
    }
}