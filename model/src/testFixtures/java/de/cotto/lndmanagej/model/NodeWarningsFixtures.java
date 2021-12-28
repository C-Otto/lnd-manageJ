package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;

public class NodeWarningsFixtures {
    public static final NodeWarnings NODE_WARNINGS = new NodeWarnings(
            NODE_ONLINE_PERCENTAGE_WARNING,
            NODE_ONLINE_CHANGES_WARNING
    );
}
