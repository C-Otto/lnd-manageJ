package de.cotto.lndmanagej.configuration;

public enum WarningsConfigurationSettings implements ConfigurationSetting {
    ONLINE_CHANGES_THRESHOLD("online_changes_threshold"),
    ONLINE_PERCENTAGE_THRESHOLD("online_percentage_threshold"),
    ONLINE_WARNING_IGNORE_NODE("online_warning_ignore_node"),

    CHANNEL_FLUCTUATION_LOWER_THRESHOLD("channel_fluctuation_lower_threshold"),
    CHANNEL_FLUCTUATION_UPPER_THRESHOLD("channel_fluctuation_upper_threshold"),
    CHANNEL_FLUCTUATION_IGNORE_CHANNEL("channel_fluctuation_warning_ignore_channel"),

    NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER("node_flow_maximum_days_to_consider"),
    NODE_FLOW_MINIMUM_DAYS_FOR_WARNING("node_flow_minimum_days_for_warning"),
    NODE_FLOW_WARNING_IGNORE_NODE("node_flow_warning_ignore_node"),

    NODE_RATING_THRESHOLD("node_rating_threshold"),
    NODE_RATING_WARNING_IGNORE_NODE("node_rating_warning_ignore_node");

    private final String name;

    WarningsConfigurationSettings(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSection() {
        return "warnings";
    }
}
