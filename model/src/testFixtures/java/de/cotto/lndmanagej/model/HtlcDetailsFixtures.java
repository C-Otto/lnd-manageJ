package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;

public class HtlcDetailsFixtures {
    public static final HtlcDetails HTLC_DETAILS = HtlcDetails.builder()
            .withIncomingChannelId(CHANNEL_ID.getShortChannelId())
            .withOutgoingChannelId(CHANNEL_ID_2.getShortChannelId())
            .withTimestamp(789)
            .withIncomingHtlcId(1)
            .withOutgoingHtlcId(2)
            .build();
}
