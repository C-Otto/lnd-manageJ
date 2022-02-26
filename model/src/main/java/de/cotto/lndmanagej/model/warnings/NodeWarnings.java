package de.cotto.lndmanagej.model.warnings;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record NodeWarnings(Set<NodeWarning> warnings) {
    public static final NodeWarnings NONE = new NodeWarnings();

    public NodeWarnings(NodeWarning... nodeWarnings) {
        this(Arrays.stream(nodeWarnings).collect(Collectors.toSet()));
    }

    public Set<String> descriptions() {
        return warnings.stream().map(NodeWarning::description).collect(Collectors.toSet());
    }
}
