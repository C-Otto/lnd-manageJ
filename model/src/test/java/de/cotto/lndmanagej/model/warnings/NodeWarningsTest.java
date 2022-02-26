package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class NodeWarningsTest {
    @Test
    void warnings() {
        assertThat(NODE_WARNINGS.warnings()).containsExactlyInAnyOrder(
                NODE_ONLINE_PERCENTAGE_WARNING,
                NODE_ONLINE_CHANGES_WARNING,
                NODE_NO_FLOW_WARNING
        );
    }

    @Test
    void descriptions() {
        assertThat(NODE_WARNINGS.descriptions()).containsExactlyInAnyOrder(
                NODE_ONLINE_PERCENTAGE_WARNING.description(),
                NODE_ONLINE_CHANGES_WARNING.description(),
                NODE_NO_FLOW_WARNING.description()
        );
    }

    @Test
    void none() {
        assertThat(NodeWarnings.NONE).isEqualTo(new NodeWarnings(Set.of()));
    }
}