package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlineChangesWarningTest {
    @Test
    void name() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.changes()).isEqualTo(123);
    }
}