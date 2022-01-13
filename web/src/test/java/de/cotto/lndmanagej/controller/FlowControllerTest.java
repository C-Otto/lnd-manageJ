package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.service.FlowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowControllerTest {
    private static final FlowReportDto FLOW_REPORT_DTO = FlowReportDto.createFromModel(FLOW_REPORT);
    @InjectMocks
    private FlowController flowController;

    @Mock
    private FlowService flowService;

    @Test
    void getFlowReportForChannel() {
        when(flowService.getFlowReportForChannel(CHANNEL_ID)).thenReturn(FLOW_REPORT);
        assertThat(flowController.getFlowReportForChannel(CHANNEL_ID)).isEqualTo(FLOW_REPORT_DTO);
    }

    @Test
    void getFlowReportForChannel_with_max_age() {
        when(flowService.getFlowReportForChannel(CHANNEL_ID, Duration.ofDays(123))).thenReturn(FLOW_REPORT);
        assertThat(flowController.getFlowReportForChannel(CHANNEL_ID, 123)).isEqualTo(FLOW_REPORT_DTO);
    }

    @Test
    void getFlowReportForPeer() {
        when(flowService.getFlowReportForPeer(PUBKEY)).thenReturn(FLOW_REPORT);
        assertThat(flowController.getFlowReportForPeer(PUBKEY)).isEqualTo(FLOW_REPORT_DTO);
    }

    @Test
    void getFlowReportForPeer_with_max_age() {
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(123))).thenReturn(FLOW_REPORT);
        assertThat(flowController.getFlowReportForPeer(PUBKEY, 123)).isEqualTo(FLOW_REPORT_DTO);
    }
}