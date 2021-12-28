package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;

public record OnlineReport(boolean online, ZonedDateTime since, int onlinePercentageLastWeek, int changesLastWeek) {
    public static OnlineReport createFromStatus(
            OnlineStatus onlineStatus,
            int onlinePercentageLastWeek,
            int changesLastWeek
    ) {
        return new OnlineReport(onlineStatus.online(), onlineStatus.since(), onlinePercentageLastWeek, changesLastWeek);
    }
}
