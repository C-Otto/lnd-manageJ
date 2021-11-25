package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/legacy")
public class LegacyController {
    private static final String NEWLINE = "\n";
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final Metrics metrics;

    public LegacyController(
            NodeService nodeService,
            ChannelService channelService,
            Metrics metrics
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.metrics = metrics;
    }

    @GetMapping("/node/{pubkey}/all-channels")
    public String getAllChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        mark("getAllChannelIdsForPubkey");
        return channelService.getAllChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .sorted()
                .map(ChannelId::toString)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels/pretty")
    public String getOpenChannelIdsPretty() {
        mark("getOpenChannelIdsPretty");
        return channelService.getOpenChannels().stream()
                .sorted(Comparator.comparing(LocalOpenChannel::getId))
                .map(localOpenChannel -> {
                    Pubkey pubkey = localOpenChannel.getRemotePubkey();
                    return localOpenChannel.getId().getCompactForm() +
                            "\t" + pubkey +
                            "\t" + localOpenChannel.getCapacity() +
                            "\t" + nodeService.getAlias(pubkey);
                })
                .collect(Collectors.joining(NEWLINE));
    }

    private void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
