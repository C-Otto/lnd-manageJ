package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.page.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;

@Controller
public class DashboardController {

    private final PageService page;

    public DashboardController(PageService pageService) {
        this.page = pageService;
    }

    @GetMapping("/")
    public String dashboard(Model model, @Nullable @RequestParam(required = false) String sort) {
        return page.dashboard(sort).create(model);
    }

    @GetMapping(path = {"/channel", "/channels"})
    public String channels(Model model, @Nullable @RequestParam(required = false) String sort) {
        return page.channels(sort).create(model);
    }

    @GetMapping(path = {"/node", "/nodes"})
    public String nodes(Model model) {
        return page.nodes().create(model);
    }

}