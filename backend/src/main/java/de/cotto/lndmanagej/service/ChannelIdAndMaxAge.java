package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;

import java.time.Duration;

record ChannelIdAndMaxAge(ChannelId channelId, Duration maxAge) {
}
