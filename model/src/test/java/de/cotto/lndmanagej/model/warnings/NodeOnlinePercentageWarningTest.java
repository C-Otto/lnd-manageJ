package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlinePercentageWarningTest {
    @Test
    void onlinePercentage() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING.onlinePercentage()).isEqualTo(51);
    }

    @Test
    void description() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING.description()).isEqualTo("Node has been online 51% in the last week");
    }
}