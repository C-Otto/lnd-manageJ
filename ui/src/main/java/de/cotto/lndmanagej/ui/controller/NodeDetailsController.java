package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.page.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.NoSuchElementException;

@Controller
public class NodeDetailsController {

    private final PageService pageService;

    public NodeDetailsController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/node/{pubkey}")
    public String nodeDetails(@PathVariable Pubkey pubkey, Model model) {
        try {
            return pageService.nodeDetails(pubkey).create(model);
        } catch (NoSuchElementException e) {
            return pageService.error("Node not found.").create(model);
        } catch (IllegalArgumentException e) {
            return pageService.error("Invalid public key.").create(model);
        }
    }

}
