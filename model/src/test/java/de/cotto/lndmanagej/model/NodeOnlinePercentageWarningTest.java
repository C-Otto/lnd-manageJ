package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlinePercentageWarningTest {
    @Test
    void onlinePercentage() {
        assertThat(NODE_ONLINE_PERCENTAGE_WARNING.onlinePercentage()).isEqualTo(51);
    }
}