package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING_2;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlinePercentageWarningTest {
    @Test
    void onlinePercentage() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING.onlinePercentage()).isEqualTo(51);
    }

    @Test
    void onlinePercentage_2() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING_2.onlinePercentage()).isEqualTo(1);
    }

    @Test
    void description() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING.description())
                .isEqualTo("Node has been online 51% in the past 14 days");
    }

    @Test
    void description_2() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING_2.description())
                .isEqualTo("Node has been online 1% in the past 21 days");
    }
}