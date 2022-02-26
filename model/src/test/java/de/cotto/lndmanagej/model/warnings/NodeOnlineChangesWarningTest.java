package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING_2;
import static org.assertj.core.api.Assertions.assertThat;

class NodeOnlineChangesWarningTest {
    @Test
    void changes() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.changes()).isEqualTo(123);
    }

    @Test
    void days() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.days()).isEqualTo(7);
    }

    @Test
    void description() {
        assertThat(NODE_ONLINE_CHANGES_WARNING.description())
                .isEqualTo("Node changed between online and offline 123 times in the past 7 days");
    }

    @Test
    void description_2() {
        assertThat(NODE_ONLINE_CHANGES_WARNING_2.description())
                .isEqualTo("Node changed between online and offline 99 times in the past 14 days");
    }
}