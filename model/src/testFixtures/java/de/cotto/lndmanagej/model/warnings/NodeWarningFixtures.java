package de.cotto.lndmanagej.model.warnings;

public class NodeWarningFixtures {
    public static final NodeOnlinePercentageWarning NODE_ONLINE_PERCENTAGE_WARNING =
            new NodeOnlinePercentageWarning(51, 14);
    public static final NodeOnlinePercentageWarning NODE_ONLINE_PERCENTAGE_WARNING_2 =
            new NodeOnlinePercentageWarning(1, 21);
    public static final NodeOnlineChangesWarning NODE_ONLINE_CHANGES_WARNING =
            new NodeOnlineChangesWarning(123, 7);
    public static final NodeOnlineChangesWarning NODE_ONLINE_CHANGES_WARNING_2 =
            new NodeOnlineChangesWarning(99, 14);
    public static final NodeNoFlowWarning NODE_NO_FLOW_WARNING = new NodeNoFlowWarning(16);
    public static final NodeRatingWarning NODE_RATING_WARNING = new NodeRatingWarning(1_234_567, 2_345_678);
}
