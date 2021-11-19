package de.cotto.lndmanagej.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.cotto.lndmanagej.model.ChannelId;

public record ChannelDetailsDto(@JsonSerialize(using = ToStringSerializer.class) ChannelId channelId) {
}
