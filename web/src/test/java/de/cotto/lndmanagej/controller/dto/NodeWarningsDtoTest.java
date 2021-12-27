package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class NodeWarningsDtoTest {
    @Test
    void createFromModel() {
        assertThat(NodeWarningsDto.createFromModel(NODE_WARNINGS))
                .isEqualTo(new NodeWarningsDto(NODE_WARNINGS.warnings()));
    }
}