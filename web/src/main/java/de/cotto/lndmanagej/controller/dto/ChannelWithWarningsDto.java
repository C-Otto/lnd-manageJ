package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;

import java.util.Set;

public record ChannelWithWarningsDto(Set<String> warnings, ChannelId channelId) {
}
