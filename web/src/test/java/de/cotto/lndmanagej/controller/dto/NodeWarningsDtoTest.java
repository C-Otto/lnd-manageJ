package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class NodeWarningsDtoTest {
    @Test
    void createFromModel() {
        assertThat(NodeWarningsDto.createFromModel(NODE_WARNINGS)).isEqualTo(new NodeWarningsDto(Set.of(
                "No flow in the past 16 days",
                "Node has been online 51% in the last week",
                "Node changed between online and offline 123 times"
        )));
    }
}