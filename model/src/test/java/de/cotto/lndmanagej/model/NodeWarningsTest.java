package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class NodeWarningsTest {
    @Test
    void warnings() {
        assertThat(NODE_WARNINGS.warnings()).containsExactly(NODE_ONLINE_PERCENTAGE_WARNING);
    }

    @Test
    void none() {
        assertThat(NodeWarnings.NONE).isEqualTo(new NodeWarnings(List.of()));
    }
}