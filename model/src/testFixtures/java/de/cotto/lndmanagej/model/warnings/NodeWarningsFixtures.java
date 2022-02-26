package de.cotto.lndmanagej.model.warnings;

import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING_2;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING_2;

public class NodeWarningsFixtures {
    public static final NodeWarnings NODE_WARNINGS = new NodeWarnings(
            NODE_ONLINE_PERCENTAGE_WARNING,
            NODE_ONLINE_CHANGES_WARNING,
            NODE_NO_FLOW_WARNING
    );

    public static final NodeWarnings NODE_WARNINGS_2 = new NodeWarnings(
            NODE_ONLINE_PERCENTAGE_WARNING_2,
            NODE_ONLINE_CHANGES_WARNING_2
    );

}
