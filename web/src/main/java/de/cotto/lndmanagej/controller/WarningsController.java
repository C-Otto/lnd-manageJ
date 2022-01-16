package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.NodeWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/")
@Import(ObjectMapperConfiguration.class)
public class WarningsController {
    private final NodeWarningsService nodeWarningsService;

    public WarningsController(NodeWarningsService nodeWarningsService) {
        this.nodeWarningsService = nodeWarningsService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/warnings")
    public NodeWarningsDto getWarningsForNode(@PathVariable Pubkey pubkey) {
        return NodeWarningsDto.createFromModel(nodeWarningsService.getNodeWarnings(pubkey));
    }

    @Timed
    @GetMapping("/warnings")
    public NodesWithWarningsDto getWarnings() {
        List<NodeWithWarningsDto> list = nodeWarningsService.getNodeWarnings().entrySet().stream()
                .map(entry -> getWarningsDto(entry.getKey(), entry.getValue()))
                .toList();
        return new NodesWithWarningsDto(list);
    }

    private NodeWithWarningsDto getWarningsDto(Node node, NodeWarnings warnings) {
        return new NodeWithWarningsDto(
                NodeWarningsDto.createFromModel(warnings).nodeWarnings(),
                node.alias(),
                node.pubkey()
        );
    }
}
