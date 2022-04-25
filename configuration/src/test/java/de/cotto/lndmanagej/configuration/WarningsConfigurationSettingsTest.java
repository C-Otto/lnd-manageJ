package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.CHANNEL_FLUCTUATION_LOWER_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.CHANNEL_FLUCTUATION_UPPER_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.MAX_NUM_UPDATES;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MINIMUM_DAYS_FOR_WARNING;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_CHANGES_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_PERCENTAGE_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;

class WarningsConfigurationSettingsTest {
    private static final String SECTION_NAME = "warnings";

    @Test
    void channelFluctuationLowerThreshold() {
        assertThat(CHANNEL_FLUCTUATION_LOWER_THRESHOLD.getSection()).isEqualTo(SECTION_NAME);
        assertThat(CHANNEL_FLUCTUATION_LOWER_THRESHOLD.getName()).isEqualTo("channel_fluctuation_lower_threshold");
    }

    @Test
    void channelFluctuationUpperThreshold() {
        assertThat(CHANNEL_FLUCTUATION_UPPER_THRESHOLD.getSection()).isEqualTo(SECTION_NAME);
        assertThat(CHANNEL_FLUCTUATION_UPPER_THRESHOLD.getName()).isEqualTo("channel_fluctuation_upper_threshold");
    }

    @Test
    void maxNumUpdates() {
        assertThat(MAX_NUM_UPDATES.getSection()).isEqualTo(SECTION_NAME);
        assertThat(MAX_NUM_UPDATES.getName()).isEqualTo("max_num_updates");
    }

    @Test
    void nodeFlowMinimumDaysForWarning() {
        assertThat(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING.getSection()).isEqualTo(SECTION_NAME);
        assertThat(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING.getName()).isEqualTo("node_flow_minimum_days_for_warning");
    }

    @Test
    void nodeFlowMaximumDaysToConsider() {
        assertThat(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER.getSection()).isEqualTo(SECTION_NAME);
        assertThat(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER.getName()).isEqualTo("node_flow_maximum_days_to_consider");
    }

    @Test
    void onlinePercentageThreshold() {
        assertThat(ONLINE_PERCENTAGE_THRESHOLD.getSection()).isEqualTo(SECTION_NAME);
        assertThat(ONLINE_PERCENTAGE_THRESHOLD.getName()).isEqualTo("online_percentage_threshold");
    }

    @Test
    void onlineChangesThreshold() {
        assertThat(ONLINE_CHANGES_THRESHOLD.getSection()).isEqualTo(SECTION_NAME);
        assertThat(ONLINE_CHANGES_THRESHOLD.getName()).isEqualTo("online_changes_threshold");
    }
}
