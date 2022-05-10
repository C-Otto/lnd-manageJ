package de.cotto.lndmanagej.ui.page.node;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

public class NodeDetailsPage extends ThymeleafPage {
    public NodeDetailsPage(NodeDetailsDto nodeDetails) {
        add("pubkey", nodeDetails.node());
        add("node", nodeDetails);
    }

    @Override
    public String getView() {
        return "node-details";
    }
}
