package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.service.ChannelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channel/{channelId}")
public class ChannelDetailsController {
    private final ChannelService channelService;
    private final Metrics metrics;

    public ChannelDetailsController(ChannelService channelService, Metrics metrics) {
        this.channelService = channelService;
        this.metrics = metrics;
    }

    @GetMapping("/details")
    public ChannelDetailsDto getChannelDetails(@PathVariable ChannelId channelId) throws NotFoundException {
        metrics.mark(MetricRegistry.name(getClass(), "getChannelDetails"));
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        return new ChannelDetailsDto(localChannel.getId());
    }
}
