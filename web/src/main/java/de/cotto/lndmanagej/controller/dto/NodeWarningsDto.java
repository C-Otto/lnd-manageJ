package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.NodeWarning;
import de.cotto.lndmanagej.model.NodeWarnings;

import java.util.List;

public record NodeWarningsDto(List<NodeWarning> nodeWarnings) {
    public static NodeWarningsDto createFromModel(NodeWarnings nodeWarnings) {
        return new NodeWarningsDto(nodeWarnings.warnings());
    }
}
