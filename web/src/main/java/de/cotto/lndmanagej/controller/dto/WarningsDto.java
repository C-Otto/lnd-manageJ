package de.cotto.lndmanagej.controller.dto;

import com.google.common.collect.Sets;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;

import java.util.Set;

public record WarningsDto(Set<String> warnings) {
    public static WarningsDto createFromModel(NodeWarnings nodeWarnings) {
        return new WarningsDto(nodeWarnings.descriptions());
    }

    public static WarningsDto createFromModel(ChannelWarnings channelWarnings) {
        return new WarningsDto(channelWarnings.descriptions());
    }

    public static WarningsDto createFromModels(NodeWarnings nodeWarnings, ChannelWarnings channelWarnings) {
        return new WarningsDto(Sets.union(nodeWarnings.descriptions(), channelWarnings.descriptions()));
    }
}
