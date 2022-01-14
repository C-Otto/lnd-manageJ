package de.cotto.lndmanagej.model;

import de.cotto.lndmanagej.model.warnings.NodeNoFlowWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;

public class NodeWarningFixtures {
    public static final NodeOnlinePercentageWarning NODE_ONLINE_PERCENTAGE_WARNING =
            new NodeOnlinePercentageWarning(51);
    public static final NodeOnlineChangesWarning NODE_ONLINE_CHANGES_WARNING =
            new NodeOnlineChangesWarning(123);
    public static final NodeNoFlowWarning NODE_NO_FLOW_WARNING =
            new NodeNoFlowWarning(16);
}
