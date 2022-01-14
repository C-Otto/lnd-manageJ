package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.NodeWarnings;

import java.util.Set;

public record NodeWarningsDto(Set<String> nodeWarnings) {
    public static NodeWarningsDto createFromModel(NodeWarnings nodeWarnings) {
        return new NodeWarningsDto(nodeWarnings.descriptions());
    }
}
