package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class WarningsDtoTest {
    @Test
    void createFromModels() {
        assertThat(WarningsDto.createFromModels(NODE_WARNINGS, CHANNEL_WARNINGS)).isEqualTo(new WarningsDto(Set.of(
                "No flow in the past 16 days",
                "Node has been online 51% in the past 14 days",
                "Node changed between online and offline 123 times in the past 7 days",
                "Channel has accumulated 101,000 updates"
        )));
    }

    @Test
    void createFromModel_node() {
        assertThat(WarningsDto.createFromModel(NODE_WARNINGS)).isEqualTo(new WarningsDto(Set.of(
                "No flow in the past 16 days",
                "Node has been online 51% in the past 14 days",
                "Node changed between online and offline 123 times in the past 7 days"
        )));
    }

    @Test
    void createFromModel_channel() {
        assertThat(WarningsDto.createFromModel(CHANNEL_WARNINGS)).isEqualTo(new WarningsDto(Set.of(
                "Channel has accumulated 101,000 updates"
        )));
    }
}