package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;

public record OnlineStatus(boolean online, ZonedDateTime since) {
}
