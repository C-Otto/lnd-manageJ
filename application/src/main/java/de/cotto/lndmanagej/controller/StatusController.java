package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status/")
@Import(ObjectMapperConfiguration.class)
public class StatusController {
    private final OwnNodeService ownNodeService;
    private final Metrics metrics;

    public StatusController(OwnNodeService ownNodeService, Metrics metrics) {
        this.ownNodeService = ownNodeService;
        this.metrics = metrics;
    }

    @GetMapping("/synced-to-chain")
    public boolean isSyncedToChain() {
        metrics.mark(MetricRegistry.name(getClass(), "isSyncedToChain"));
        return ownNodeService.isSyncedToChain();
    }

}
