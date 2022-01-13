package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.FlowService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@Import(ObjectMapperConfiguration.class)
public class FlowController {

    private final FlowService flowService;

    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @Timed
    @GetMapping("/channel/{channelId}/flow-report")
    public FlowReportDto getFlowReportForChannel(@PathVariable ChannelId channelId) {
        return FlowReportDto.createFromModel(flowService.getFlowReportForChannel(channelId));
    }

    @Timed
    @GetMapping("/channel/{channelId}/flow-report/last-days/{lastDays}")
    public FlowReportDto getFlowReportForChannel(@PathVariable ChannelId channelId, @PathVariable int lastDays) {
        return FlowReportDto.createFromModel(flowService.getFlowReportForChannel(channelId, Duration.ofDays(lastDays)));
    }

    @Timed
    @GetMapping("/node/{pubkey}/flow-report")
    public FlowReportDto getFlowReportForPeer(@PathVariable Pubkey pubkey) {
        return FlowReportDto.createFromModel(flowService.getFlowReportForPeer(pubkey));
    }

    @Timed
    @GetMapping("/node/{pubkey}/flow-report/last-days/{lastDays}")
    public FlowReportDto getFlowReportForPeer(@PathVariable Pubkey pubkey, @PathVariable int lastDays) {
        return FlowReportDto.createFromModel(flowService.getFlowReportForPeer(pubkey, Duration.ofDays(lastDays)));
    }
}
