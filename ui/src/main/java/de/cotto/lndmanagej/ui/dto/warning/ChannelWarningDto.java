package de.cotto.lndmanagej.ui.dto.warning;

import de.cotto.lndmanagej.model.ChannelId;

public record ChannelWarningDto(ChannelId channelId, String description) {
}