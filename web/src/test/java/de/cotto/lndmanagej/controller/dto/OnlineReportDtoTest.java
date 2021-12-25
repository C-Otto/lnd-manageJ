package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class OnlineReportDtoTest {
    @Test
    void createFromModel() {
        assertThat(OnlineReportDto.createFromModel(ONLINE_REPORT)).isEqualTo(
                new OnlineReportDto(
                        ONLINE_REPORT.online(),
                        ONLINE_REPORT.since().toString(),
                        ONLINE_REPORT.onlinePercentageLastWeek()
                )
        );
    }

    @Test
    void since() {
        assertThat(OnlineReportDto.createFromModel(ONLINE_REPORT).since()).isEqualTo("2021-12-23T01:02:03Z");
    }
}