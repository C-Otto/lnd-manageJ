package de.cotto.lndmanagej.model;

import java.time.Duration;

public record ChannelIdAndMaxAge(ChannelId channelId, Duration maxAge) {
}
