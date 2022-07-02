package de.cotto.lndmanagej.ui.page.node;

import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.List;

public class NodesPage extends ThymeleafPage {

    private final List<NodeDto> nodes;

    public NodesPage(List<NodeDto> nodes) {
        super();
        this.nodes = sort(nodes);
        add("nodes", this.nodes);
    }

    private List<NodeDto> sort(List<NodeDto> nodes) {
        return nodes.stream()
                .sorted(NodeDto.getDefaultComparator())
                .toList();
    }

    @Override
    public String getView() {
        return "nodes";
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }
}
