package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

public record PendingOpenChannelDto(
        String remoteAlias,
        Pubkey remotePubkey,
        long capacitySat,
        boolean privateChannel,
        OpenInitiator initiator
) {
}
