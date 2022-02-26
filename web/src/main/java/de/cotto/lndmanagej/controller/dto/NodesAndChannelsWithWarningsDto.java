package de.cotto.lndmanagej.controller.dto;

import java.util.List;

public record NodesAndChannelsWithWarningsDto(
        List<NodeWithWarningsDto> nodesWithWarnings,
        List<ChannelWithWarningsDto> channelsWithWarnings
) {
    public static final NodesAndChannelsWithWarningsDto NONE =
            new NodesAndChannelsWithWarningsDto(List.of(), List.of());
}
