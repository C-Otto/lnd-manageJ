package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

public class OnlineStatusFixtures {
    private static final ZonedDateTime TIMESTAMP = LocalDateTime.of(2021, 12, 23, 1, 2, 3).atZone(UTC);

    public static final OnlineStatus ONLINE_STATUS = new OnlineStatus(
            true,
            TIMESTAMP
    );

    public static final OnlineStatus ONLINE_STATUS_OFFLINE = new OnlineStatus(
            false,
            TIMESTAMP
    );
}
