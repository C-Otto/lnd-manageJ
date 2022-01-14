package de.cotto.lndmanagej.model.warnings;

import de.cotto.lndmanagej.model.NodeWarnings;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;

public class NodeWarningsFixtures {
    public static final NodeWarnings NODE_WARNINGS = new NodeWarnings(
            NODE_ONLINE_PERCENTAGE_WARNING,
            NODE_ONLINE_CHANGES_WARNING,
            NODE_NO_FLOW_WARNING
    );
}
