package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.CloseInitiator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClosedChannelDetailsDtoTest {
    @Test
    void initiator() {
        assertThat(new ClosedChannelDetailsDto(CloseInitiator.LOCAL, 987_654).initiator()).isEqualTo("LOCAL");
    }
}