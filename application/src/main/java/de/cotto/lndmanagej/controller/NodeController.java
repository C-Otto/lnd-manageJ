package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/node/{pubkey}")
public class NodeController {
    private final NodeService nodeService;
    private final Metrics metrics;

    public NodeController(NodeService nodeService, Metrics metrics) {
        this.nodeService = nodeService;
        this.metrics = metrics;
    }

    @GetMapping("/alias")
    public String getAlias(Pubkey pubkey) {
        metrics.mark(MetricRegistry.name(getClass(), "getAlias"));
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/details")
    public NodeDetailsDto getDetails(@PathVariable Pubkey pubkey) {
        metrics.mark(MetricRegistry.name(getClass(), "getDetails"));
        return new NodeDetailsDto(pubkey, getAlias(pubkey));
    }
}
