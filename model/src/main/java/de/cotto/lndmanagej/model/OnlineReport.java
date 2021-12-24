package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;

public record OnlineReport(boolean online, ZonedDateTime since) {
    public static OnlineReport createFromStatus(OnlineStatus onlineStatus) {
        return new OnlineReport(onlineStatus.online(), onlineStatus.since());
    }
}
