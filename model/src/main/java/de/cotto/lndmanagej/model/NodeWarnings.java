package de.cotto.lndmanagej.model;

import java.util.Arrays;
import java.util.List;

public record NodeWarnings(List<NodeWarning> warnings) {
    public static final NodeWarnings NONE = new NodeWarnings();

    public NodeWarnings(NodeWarning... nodeWarnings) {
        this(Arrays.stream(nodeWarnings).toList());
    }
}
