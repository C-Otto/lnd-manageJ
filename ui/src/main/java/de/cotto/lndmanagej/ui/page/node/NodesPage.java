package de.cotto.lndmanagej.ui.page.node;

import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.List;

public final class NodesPage extends ThymeleafPage {

    private final List<NodeDto> nodes;

    public NodesPage(List<NodeDto> nodes) {
        super();
        this.nodes = nodes;
        add("nodes", this.nodes);
    }

    @Override
    public String getView() {
        return "nodes-page";
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }
}
