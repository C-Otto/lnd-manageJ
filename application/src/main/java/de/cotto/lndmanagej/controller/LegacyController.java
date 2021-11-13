package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/legacy")
public class LegacyController {
    private static final String NEWLINE = "\n";
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final FeeService feeService;
    private final BalanceService balanceService;
    private final Metrics metrics;

    public LegacyController(
            NodeService nodeService,
            ChannelService channelService,
            OwnNodeService ownNodeService,
            FeeService feeService,
            BalanceService balanceService,
            Metrics metrics
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.feeService = feeService;
        this.balanceService = balanceService;
        this.metrics = metrics;
    }

    @GetMapping("/node/{pubkey}/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        mark("getAlias");
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/node/{pubkey}/open-channels")
    public String getOpenChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        mark("getOpenChannelIdsForPubkey");
        return channelService.getOpenChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .sorted()
                .map(ChannelId::toString)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels")
    public String getOpenChannelIds() {
        mark("getOpenChannelIds");
        return getOpenChannelIdsSorted()
                .map(ChannelId::toString)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels/compact")
    public String getOpenChannelIdsCompact() {
        mark("getOpenChannelIdsCompact");
        return getOpenChannelIdsSorted()
                .map(ChannelId::getCompactForm)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels/pretty")
    public String getOpenChannelIdsPretty() {
        mark("getOpenChannelIdsPretty");
        return channelService.getOpenChannels().stream()
                .sorted(Comparator.comparing(LocalChannel::getId))
                .map(localChannel -> {
                    Pubkey pubkey = localChannel.getRemotePubkey();
                    return localChannel.getId().getCompactForm() +
                            "\t" + pubkey +
                            "\t" + localChannel.getCapacity() +
                            "\t" + nodeService.getAlias(pubkey);
                })
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/peer-pubkeys")
    public String getPeerPubkeys() {
        mark("getPeerPubkeys");
        return channelService.getOpenChannels().stream()
                .map(LocalChannel::getRemotePubkey)
                .map(Pubkey::toString)
                .sorted()
                .distinct()
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/synced-to-chain")
    public boolean syncedToChain() {
        mark("syncedToChain");
        return ownNodeService.isSyncedToChain();
    }

    @GetMapping("/channel/{channelId}/incoming-fee-rate")
    public long getIncomingFeeRate(@PathVariable ChannelId channelId) {
        mark("getIncomingFeeRate");
        return feeService.getIncomingFeeRate(channelId);
    }

    @GetMapping("/channel/{channelId}/outgoing-fee-rate")
    public long getOutgoingFeeRate(@PathVariable ChannelId channelId) {
        mark("getOutgoingFeeRate");
        return feeService.getOutgoingFeeRate(channelId);
    }

    @GetMapping("/channel/{channelId}/incoming-base-fee")
    public long getIncomingBaseFee(@PathVariable ChannelId channelId) {
        mark("getIncomingBaseFee");
        return feeService.getIncomingBaseFee(channelId).milliSatoshis();
    }

    @GetMapping("/channel/{channelId}/outgoing-base-fee")
    public long getOutgoingBaseFee(@PathVariable ChannelId channelId) {
        mark("getOutgoingBaseFee");
        return feeService.getOutgoingBaseFee(channelId).milliSatoshis();
    }

    @GetMapping("/channel/{channelId}/available-local-balance")
    public long getAvailableLocalBalance(@PathVariable ChannelId channelId) {
        mark("getAvailableLocalBalance");
        return balanceService.getAvailableLocalBalance(channelId).satoshis();
    }

    @GetMapping("/channel/{channelId}/available-remote-balance")
    public long getAvailableRemoteBalance(@PathVariable ChannelId channelId) {
        mark("getAvailableRemoteBalance");
        return balanceService.getAvailableRemoteBalance(channelId).satoshis();
    }

    private Stream<ChannelId> getOpenChannelIdsSorted() {
        return channelService.getOpenChannels().stream()
                .map(Channel::getId)
                .sorted();
    }

    private void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
