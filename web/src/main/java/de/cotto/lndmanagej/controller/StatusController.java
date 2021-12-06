package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.PubkeysDto;
import de.cotto.lndmanagej.model.ChannelId;
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

@RestController
@RequestMapping("/api/status/")
@Import(ObjectMapperConfiguration.class)
public class StatusController {
    private final OwnNodeService ownNodeService;
    private final ChannelService channelService;

    public StatusController(OwnNodeService ownNodeService, ChannelService channelService) {
        this.ownNodeService = ownNodeService;
        this.channelService = channelService;
    }

    @Timed
    @GetMapping("/synced-to-chain")
    public boolean isSyncedToChain() {
        return ownNodeService.isSyncedToChain();
    }

    @Timed
    @GetMapping("/block-height")
    public int getBlockHeight() {
        return ownNodeService.getBlockHeight();
    }

    @Timed
    @GetMapping("/open-channels/")
    public ChannelsDto getOpenChannels() {
        List<ChannelId> channelIds = channelService.getOpenChannels().stream()
                .map(LocalOpenChannel::getId)
                .sorted()
                .distinct()
                .toList();
        return new ChannelsDto(channelIds);
    }

    @Timed
    @GetMapping("/open-channels/pubkeys")
    public PubkeysDto getPubkeysForOpenChannels() {
        List<Pubkey> pubkeys = channelService.getOpenChannels().stream()
                .map(LocalOpenChannel::getRemotePubkey)
                .sorted()
                .distinct()
                .toList();
        return new PubkeysDto(pubkeys);
    }

    @Timed
    @GetMapping("/all-channels/")
    public ChannelsDto getAllChannels() {
        List<ChannelId> channelIds = channelService.getAllLocalChannels()
                .map(LocalChannel::getId)
                .sorted()
                .distinct()
                .toList();
        return new ChannelsDto(channelIds);
    }

    @Timed
    @GetMapping("/all-channels/pubkeys")
    public PubkeysDto getPubkeysForAllChannels() {
        List<Pubkey> pubkeys = channelService.getAllLocalChannels()
                .map(LocalChannel::getRemotePubkey)
                .sorted()
                .distinct()
                .toList();
        return new PubkeysDto(pubkeys);
    }

}
