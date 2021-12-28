package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.OnlineReport;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class OnlineReportDtoTest {
    @Test
    void createFromModel() {
        assertThat(OnlineReportDto.createFromModel(ONLINE_REPORT)).isEqualTo(
                new OnlineReportDto(
                        ONLINE_REPORT.online(),
                        ONLINE_REPORT.since().toString(),
                        ONLINE_REPORT.onlinePercentageLastWeek(),
                        ONLINE_REPORT.changesLastWeek()
                )
        );
    }

    @Test
    void since() {
        assertThat(OnlineReportDto.createFromModel(ONLINE_REPORT).since()).isEqualTo("2021-12-23T01:02:03Z");
    }

    @Test
    void since_zero_seconds() {
        ZonedDateTime timeWithZeroSeconds = ZonedDateTime.of(2021, 12, 23, 1, 2, 0, 0, ZoneOffset.UTC);
        OnlineReport onlineReport = new OnlineReport(true, timeWithZeroSeconds, 77, 123);
        assertThat(OnlineReportDto.createFromModel(onlineReport).since()).isEqualTo("2021-12-23T01:02:00Z");
    }
}