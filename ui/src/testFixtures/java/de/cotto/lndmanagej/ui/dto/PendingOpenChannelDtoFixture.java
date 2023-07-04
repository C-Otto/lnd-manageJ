package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.OpenInitiator;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class PendingOpenChannelDtoFixture {

    public static final long CAPACITY_SAT = 21_000_000;

    public static final PendingOpenChannelDto PENDING_OPEN_CHANNEL_DTO = new PendingOpenChannelDto(
            "Albert",
            PUBKEY_2,
            CAPACITY_SAT,
            false,
            OpenInitiator.LOCAL
    );

}
