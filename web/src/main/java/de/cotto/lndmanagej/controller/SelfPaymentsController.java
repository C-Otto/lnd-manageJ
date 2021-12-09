package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.SelfPaymentDto;
import de.cotto.lndmanagej.dto.SelfPaymentsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.SelfPaymentsService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@Import(ObjectMapperConfiguration.class)
public class SelfPaymentsController {
    private final SelfPaymentsService selfPaymentsService;

    public SelfPaymentsController(SelfPaymentsService selfPaymentsService) {
        this.selfPaymentsService = selfPaymentsService;
    }

    @Timed
    @GetMapping("/channel/{channelId}/self-payments-from-channel")
    public SelfPaymentsDto getSelfPaymentsFromChannel(@PathVariable ChannelId channelId) {
        return new SelfPaymentsDto(selfPaymentsService.getSelfPaymentsFromChannel(channelId).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList());
    }

    @Timed
    @GetMapping("/node/{pubkey}/self-payments-from-peer")
    public SelfPaymentsDto getSelfPaymentsFromPeer(@PathVariable Pubkey pubkey) {
        return new SelfPaymentsDto(selfPaymentsService.getSelfPaymentsFromPeer(pubkey).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList());
    }

    @Timed
    @GetMapping("/channel/{channelId}/self-payments-to-channel")
    public SelfPaymentsDto getSelfPaymentsToChannel(@PathVariable ChannelId channelId) {
        return new SelfPaymentsDto(selfPaymentsService.getSelfPaymentsToChannel(channelId).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList());
    }

    @Timed
    @GetMapping("/node/{pubkey}/self-payments-to-peer")
    public SelfPaymentsDto getSelfPaymentsToPeer(@PathVariable Pubkey pubkey) {
        return new SelfPaymentsDto(selfPaymentsService.getSelfPaymentsToPeer(pubkey).stream()
                .map(SelfPaymentDto::createFromModel)
                .toList());
    }
}
