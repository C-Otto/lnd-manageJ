package de.cotto.lndmanagej.ui.page.node;

import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.List;

public class NodesPage extends ThymeleafPage {

    public NodesPage(List<NodeDto> nodes) {
        super();
        add("nodes", nodes);
    }

    @Override
    public String getView() {
        return "nodes";
    }
}
