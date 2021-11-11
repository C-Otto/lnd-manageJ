package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/node/{pubkey}/")
public class NodeController {
    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/open-channels")
    public List<Long> getOpenChannelIds(@PathVariable Pubkey pubkey) {
        return nodeService.getOpenChannelIds(pubkey).stream()
                .map(ChannelId::shortChannelId)
                .collect(Collectors.toList());
    }
}
