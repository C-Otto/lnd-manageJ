package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final PolicyService policyService;

    public LegacyController(
            NodeService nodeService,
            ChannelService channelService,
            PolicyService policyService
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.policyService = policyService;
    }

    @Timed
    @GetMapping("/open-channels/pretty")
    public String getOpenChannelIdsPretty() {
        return channelService.getOpenChannels().stream()
                .sorted(Comparator.comparing(LocalOpenChannel::getId))
                .map(localOpenChannel -> {
                    Pubkey pubkey = localOpenChannel.getRemotePubkey();
                    long ppm = policyService.getPolicyTo(localOpenChannel.getId(), pubkey)
                            .map(Policy::feeRate)
                            .orElse(0L);
                    return localOpenChannel.getId().getCompactForm() +
                            "\t" + pubkey +
                            "\t" + localOpenChannel.getCapacity().toStringSat() +
                            "\t" + localOpenChannel.getBalanceInformation().localAvailable().toStringSat() +
                            "\t" + ppm +
                            "\t" + nodeService.getAlias(pubkey);
                })
                .collect(Collectors.joining(NEWLINE));
    }
}
