package de.cotto.lndmanagej.controller.dto;

import java.util.List;

public record NodesWithWarningsDto(List<NodeWithWarningsDto> nodesWithWarnings) {
    public static final NodesWithWarningsDto NONE = new NodesWithWarningsDto(List.of());
}
