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

    private final PageService page;

    public NodeDetailsController(PageService pageService) {
        this.page = pageService;
    }

    @GetMapping("/node/{pubkey}")
    public String nodeDetails(@PathVariable Pubkey pubkey, Model model) {
        try {
            return page.nodeDetails(pubkey).create(model);
        } catch (NoSuchElementException e) {
            return page.error("Node not found.").create(model);
        } catch (IllegalArgumentException e) {
            return page.error("Invalid public key.").create(model);
        }
    }

}
