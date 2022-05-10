package de.cotto.lndmanagej.ui.controller;

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
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private static final int SINGLE_NODE = 1;

    private final UiDataService dataService;
    private final PageService page;

    public SearchController(UiDataService dataService, PageService pageService) {
        this.dataService = dataService;
        this.page = pageService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q") String query, Model model) {
        List<OpenChannelDto> openChannels = dataService.getOpenChannels();

        Optional<OpenChannelDto> openChannel = openChannels.stream()
                .filter(channel -> String.valueOf(channel.channelId().getShortChannelId()).equals(query)
                        || channel.channelId().toString().equals(query))
                .findFirst();

        if (openChannel.isPresent()) {

            return detailsPage(extractChannelId(query), model);
        }

        openChannel = openChannels.stream()
                .filter(c -> c.remotePubkey().toString().equals(query))
                .findFirst();

        if (openChannel.isPresent()) {
            return page.nodeDetails(Pubkey.create(query)).create(model);
        }

        List<OpenChannelDto> matchingChannels = openChannels.stream()
                .filter(chan -> chan.remoteAlias().toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        if (matchingChannels.isEmpty()) {
            return page.error("No search result.").create(model);

        }

        if (matchingChannels.size() == SINGLE_NODE) {
            return page.nodeDetails(matchingChannels.get(0).remotePubkey()).create(model);
        }

        return page.nodes(matchingChannels).create(model);
    }

    private String detailsPage(ChannelId channelId, Model model) {
        try {
            return page.channelDetails(channelId).create(model);
        } catch (NotFoundException e) {
            return page.error("Channel not found.").create(model);
        }
    }

    private ChannelId extractChannelId(String query) {
        return query.contains("x")
                ? ChannelId.fromCompactForm(query) : ChannelId.fromShortChannelId(Long.parseLong(query));
    }

}