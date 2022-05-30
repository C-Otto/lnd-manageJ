package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.StatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatusPageController {

    private final StatusService statusService;

    public StatusPageController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public String status(Model model) {
        model.addAttribute("status", statusService.getStatus());
        return "status";
    }
}