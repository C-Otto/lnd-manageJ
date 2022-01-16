package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

public class OnlineReportFixtures {
    public static final ZonedDateTime TIMESTAMP = LocalDateTime.of(2021, 12, 23, 1, 2, 3).atZone(UTC);
    public static final OnlineReport ONLINE_REPORT = new OnlineReport(true, TIMESTAMP, 77, 7, 5, 7);
    public static final OnlineReport ONLINE_REPORT_OFFLINE = new OnlineReport(false, TIMESTAMP, 85, 7, 123, 7);
}
