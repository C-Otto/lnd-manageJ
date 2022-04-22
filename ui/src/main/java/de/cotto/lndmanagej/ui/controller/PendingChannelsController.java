package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.page.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PendingChannelsController {

    private final PageService page;

    public PendingChannelsController(PageService pageService) {
        this.page = pageService;
    }

    @GetMapping("/pending-channels")
    public String pendingChannelsPage(Model model) {
        return page.pendingChannels().create(model);
    }
}