package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class SearchController {

    private static final int SINGLE_NODE = 1;

    private final UiDataService dataService;
    private final PageService pageService;
    private final ChannelIdConverter channelIdConverter;

    public SearchController(
            UiDataService dataService,
            PageService pageService,
            ChannelIdConverter channelIdConverter
    ) {
        this.dataService = dataService;
        this.pageService = pageService;
        this.channelIdConverter = channelIdConverter;
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String query, Model model) {
        List<OpenChannelDto> openChannels = dataService.getOpenChannels();

        ChannelId channelId = getForChannelId(query, openChannels).orElse(null);
        if (channelId != null) {
            return detailsPage(channelId, model);
        }

        Pubkey pubkey = getForPubkey(query, openChannels).orElse(null);
        if (pubkey != null) {
            return pageService.nodeDetails(pubkey).create(model);
        }

        String lowercaseQuery = query.toLowerCase(Locale.US);
        List<OpenChannelDto> matchingChannels = openChannels.stream()
                .filter(channel -> containsInPeerAlias(channel, lowercaseQuery))
                .toList();

        if (matchingChannels.isEmpty()) {
            return pageService.error("No search result.").create(model);
        }

        if (matchingChannels.size() == SINGLE_NODE) {
            return pageService.nodeDetails(matchingChannels.get(0).remotePubkey()).create(model);
        }

        return pageService.nodes(matchingChannels).create(model);
    }

    private Optional<ChannelId> getForChannelId(String query, List<OpenChannelDto> openChannels) {
        ChannelId channelId = channelIdConverter.tryToConvert(query).orElse(null);
        if (channelId == null) {
            return Optional.empty();
        }
        return openChannels.stream()
                .map(OpenChannelDto::channelId)
                .filter(channelId::equals)
                .findFirst();
    }

    private Optional<Pubkey> getForPubkey(String query, List<OpenChannelDto> openChannels) {
        try {
            Pubkey pubkey = Pubkey.create(query);
            return openChannels.stream()
                    .map(OpenChannelDto::remotePubkey)
                    .filter(pubkey::equals)
                    .findFirst();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private boolean containsInPeerAlias(OpenChannelDto channel, String lowercaseQuery) {
        return channel.remoteAlias().toLowerCase(Locale.US).contains(lowercaseQuery);
    }

    private String detailsPage(ChannelId channelId, Model model) {
        try {
            return pageService.channelDetails(channelId).create(model);
        } catch (NotFoundException e) {
            return pageService.error("Channel not found.").create(model);
        }
    }
}
