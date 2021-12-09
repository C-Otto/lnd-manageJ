package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.SelfPaymentDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.service.SelfPaymentsService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/")
@Import(ObjectMapperConfiguration.class)
public class SelfPaymentsController {
    private final SelfPaymentsService selfPaymentsService;

    public SelfPaymentsController(SelfPaymentsService selfPaymentsService) {
        this.selfPaymentsService = selfPaymentsService;
    }

    @Timed
    @GetMapping("/channel/{channelId}/self-payments-to-channel")
    public List<SelfPaymentDto> getSelfPaymentsToChannel(@PathVariable ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsToChannel(channelId).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList();
    }

    @Timed
    @GetMapping("/channel/{channelId}/self-payments-from-channel")
    public List<SelfPaymentDto> getSelfPaymentsFromChannel(@PathVariable ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsFromChannel(channelId).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList();
    }
}
