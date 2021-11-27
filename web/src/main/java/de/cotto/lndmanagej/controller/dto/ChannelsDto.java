package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;

import java.util.List;

public record ChannelsDto(List<ChannelId> channels) {
}
