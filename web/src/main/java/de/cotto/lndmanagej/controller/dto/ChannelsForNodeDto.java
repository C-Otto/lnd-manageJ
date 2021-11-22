package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.List;

public record ChannelsForNodeDto(
        Pubkey node,
        List<ChannelId> channels
) {
}
