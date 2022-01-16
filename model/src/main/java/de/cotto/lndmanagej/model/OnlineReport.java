package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;

public record OnlineReport(
        boolean online,
        ZonedDateTime since,
        int onlinePercentage,
        int daysForOnlinePercentage,
        int changes,
        int daysForChanges
) {
    public static OnlineReport createFromStatus(
            OnlineStatus onlineStatus,
            int onlinePercentage,
            int daysForOnlinePercentage,
            int changes,
            int daysForChanges
    ) {
        return new OnlineReport(
                onlineStatus.online(),
                onlineStatus.since(),
                onlinePercentage,
                daysForOnlinePercentage,
                changes,
                daysForChanges
        );
    }
}
