package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.PubkeysDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/status/")
@Import(ObjectMapperConfiguration.class)
public class StatusController {
    private final OwnNodeService ownNodeService;
    private final ChannelService channelService;
    private final Metrics metrics;

    public StatusController(OwnNodeService ownNodeService, ChannelService channelService, Metrics metrics) {
        this.ownNodeService = ownNodeService;
        this.channelService = channelService;
        this.metrics = metrics;
    }

    @GetMapping("/synced-to-chain")
    public boolean isSyncedToChain() {
        mark("isSyncedToChain");
        return ownNodeService.isSyncedToChain();
    }

    @GetMapping("/open-channels/pubkeys")
    public PubkeysDto getPubkeysForOpenChannels() {
        mark("getPubkeysForOpenChannels");
        List<Pubkey> pubkeys = channelService.getOpenChannels().stream()
                .map(LocalOpenChannel::getRemotePubkey)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        return new PubkeysDto(pubkeys);
    }

    @GetMapping("/all-channels/pubkeys")
    public PubkeysDto getPubkeysForAllChannels() {
        mark("getPubkeysForAllChannels");
        List<Pubkey> pubkeys = channelService.getAllLocalChannels()
                .map(LocalChannel::getRemotePubkey)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        return new PubkeysDto(pubkeys);
    }

    private void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
