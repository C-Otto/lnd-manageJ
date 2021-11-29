package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;

public class ForwardingEventFixtures {
    public static final ForwardingEvent FORWARDING_EVENT = new ForwardingEvent(
            1,
            Coins.ofMilliSatoshis(1_000),
            Coins.ofMilliSatoshis(900),
            CHANNEL_ID,
            CHANNEL_ID_2,
            LocalDateTime.of(2021, 11, 29, 18, 30, 0)
    );

    public static final ForwardingEvent FORWARDING_EVENT_2 = new ForwardingEvent(
            2,
            Coins.ofMilliSatoshis(2_000),
            Coins.ofMilliSatoshis(2_000),
            CHANNEL_ID_3,
            CHANNEL_ID,
            LocalDateTime.of(2021, 11, 29, 18, 30, 1)
    );
}
