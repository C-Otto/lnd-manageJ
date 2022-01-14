package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlineChangesWarningTest {
    @Test
    void name() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.changes()).isEqualTo(123);
    }

    @Test
    void description() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.description())
                .isEqualTo("Node changed between online and offline 123 times");
    }
}