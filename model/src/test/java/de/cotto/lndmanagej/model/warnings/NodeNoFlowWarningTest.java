package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeNoFlowWarningTest {
    @Test
    void days() {
        assertThat(NODE_NO_FLOW_WARNING.days()).isEqualTo(16);
    }

    @Test
    void description() {
        assertThat(NODE_NO_FLOW_WARNING.description()).isEqualTo("No flow in the past 16 days");
    }
}