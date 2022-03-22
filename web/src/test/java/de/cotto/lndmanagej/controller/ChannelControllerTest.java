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
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelDetailsService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelControllerTest {
    private static final PoliciesDto POLICIES_DTO = PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL);
    private static final ClosedChannelDetailsDto CLOSED_CHANNEL_DETAILS_DTO =
            ClosedChannelDetailsDto.createFromModel(CLOSED_CHANNEL);
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @InjectMocks
    private ChannelController channelController;

    @Mock
    private ChannelService channelService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private PolicyService policyService;

    @Mock
    private FeeService feeService;

    @Mock
    private ChannelDetailsService channelDetailsService;

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
    }

    @Test
    void getBasicInformation_closed_channel() throws NotFoundException {
        ChannelDto basicInformation = new ChannelDto(CLOSED_CHANNEL);
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
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(channelDetailsService.getDetails(LOCAL_OPEN_CHANNEL)).thenReturn(CHANNEL_DETAILS);

        assertThat(channelController.getDetails(CHANNEL_ID))
                .isEqualTo(ChannelDetailsDto.createFromModel(CHANNEL_DETAILS));
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
        when(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).thenReturn(POLICIES_FOR_LOCAL_CHANNEL);
        assertThat(channelController.getPolicies(CHANNEL_ID)).isEqualTo(POLICIES_DTO);
    }

    @Test
    void getPolicies_waiting_close() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(WAITING_CLOSE_CHANNEL));
        assertThat(channelController.getPolicies(CHANNEL_ID))
                .isEqualTo(PoliciesDto.createFromModel(PoliciesForLocalChannel.UNKNOWN));
    }

    @Test
    void getPolicies_channel_not_found() {
        assertThat(channelController.getPolicies(CHANNEL_ID))
                .isEqualTo(PoliciesDto.createFromModel(PoliciesForLocalChannel.UNKNOWN));
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
}
