package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public LegacyController(
            NodeService nodeService,
            ChannelService channelService,
            OwnNodeService ownNodeService,
            FeeService feeService
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.feeService = feeService;
    }

    @GetMapping("/node/{pubkey}/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/node/{pubkey}/open-channels")
    public String getOpenChannelIds(@PathVariable Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).stream()
                .map(Channel::getId)
                .sorted()
                .map(ChannelId::toString)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels")
    public String getOpenChannelIds() {
        return getOpenChannelIdsSorted()
                .map(ChannelId::toString)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/open-channels/compact")
    public String getOpenChannelIdsCompact() {
        return getOpenChannelIdsSorted()
                .map(ChannelId::getCompactForm)
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/peer-pubkeys")
    public String getPeerPubkeys() {
        return channelService.getOpenChannels().stream()
                .map(LocalChannel::getRemotePubkey)
                .map(Pubkey::toString)
                .sorted()
                .distinct()
                .collect(Collectors.joining(NEWLINE));
    }

    @GetMapping("/synced-to-chain")
    public boolean syncedToChain() {
        return ownNodeService.isSyncedToChain();
    }

    @GetMapping("/channel/{channelId}/incoming-fee-rate")
    public long getIncomingFeeRate(@PathVariable ChannelId channelId) {
        return feeService.getIncomingFeeRate(channelId);
    }

    @GetMapping("/channel/{channelId}/outgoing-fee-rate")
    public long getOutgoingFeeRate(@PathVariable ChannelId channelId) {
        return feeService.getOutgoingFeeRate(channelId);
    }

    @GetMapping("/channel/{channelId}/incoming-base-fee")
    public long getIncomingBaseFee(@PathVariable ChannelId channelId) {
        return feeService.getIncomingBaseFee(channelId).milliSatoshis();
    }

    @GetMapping("/channel/{channelId}/outgoing-base-fee")
    public long getOutgoingBaseFee(@PathVariable ChannelId channelId) {
        return feeService.getOutgoingBaseFee(channelId).milliSatoshis();
    }

    private Stream<ChannelId> getOpenChannelIdsSorted() {
        return channelService.getOpenChannels().stream()
                .map(Channel::getId)
                .sorted();
    }
}
