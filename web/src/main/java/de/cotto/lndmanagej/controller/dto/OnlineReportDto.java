package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.OnlineReport;

import java.time.format.DateTimeFormatter;

public record OnlineReportDto(
        boolean online,
        String since,
        int onlinePercentage,
        int daysForOnlinePercentage,
        int changes,
        int daysForChanges
) {
    public static OnlineReportDto createFromModel(OnlineReport onlineReport) {
        String formattedDateTime = onlineReport.since().format(DateTimeFormatter.ISO_INSTANT);
        return new OnlineReportDto(
                onlineReport.online(),
                formattedDateTime,
                onlineReport.onlinePercentage(),
                onlineReport.daysForOnlinePercentage(),
                onlineReport.changes(),
                onlineReport.daysForChanges()
        );
    }
}
