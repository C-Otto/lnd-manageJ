package de.cotto.lndmanagej.model.warnings;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record ChannelWarnings(Set<ChannelWarning> warnings) {
    public static final ChannelWarnings NONE = new ChannelWarnings();

    public ChannelWarnings(ChannelWarning... channelWarnings) {
        this(Arrays.stream(channelWarnings).collect(Collectors.toSet()));
    }

    public Set<String> descriptions() {
        return warnings.stream().map(ChannelWarning::description).collect(Collectors.toSet());
    }
}
