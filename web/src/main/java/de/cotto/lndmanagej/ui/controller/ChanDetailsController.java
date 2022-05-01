package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.ui.page.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChanDetailsController {

    private final PageService page;

    public ChanDetailsController(PageService pageService) {
        this.page = pageService;
    }

    @GetMapping("/channel/{id}")
    public String channelDetails(@PathVariable(name = "id") long id, Model model) {
        try {
            ChannelId channelId = ChannelId.fromShortChannelId(id);
            return page.channelDetails(channelId).create(model);
        } catch (NotFoundException e) {
            return page.error("Channel not found.").create(model);
        }
    }

}