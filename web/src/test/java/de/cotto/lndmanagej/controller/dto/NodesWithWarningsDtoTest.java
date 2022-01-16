package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodesWithWarningsDtoTest {
    @Test
    void none() {
        assertThat(NodesWithWarningsDto.NONE).isEqualTo(new NodesWithWarningsDto(List.of()));
    }
}