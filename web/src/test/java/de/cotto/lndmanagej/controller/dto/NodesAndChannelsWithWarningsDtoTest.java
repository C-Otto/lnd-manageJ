package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodesAndChannelsWithWarningsDtoTest {
    @Test
    void none() {
        assertThat(NodesAndChannelsWithWarningsDto.NONE)
                .isEqualTo(new NodesAndChannelsWithWarningsDto(List.of(), List.of()));
    }
}