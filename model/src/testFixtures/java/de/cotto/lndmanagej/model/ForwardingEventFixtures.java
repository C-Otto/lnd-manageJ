package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;

public class ForwardingEventFixtures {
    public static final ForwardingEvent FORWARDING_EVENT = new ForwardingEvent(
            1,
            Coins.ofMilliSatoshis(1_000),
            Coins.ofMilliSatoshis(900),
            CHANNEL_ID,
            CHANNEL_ID_2,
            LocalDateTime.of(2021, 11, 29, 18, 30, 0, 500_000_000)
    );

    public static final ForwardingEvent FORWARDING_EVENT_2 = new ForwardingEvent(
            2,
            Coins.ofMilliSatoshis(2_001),
            Coins.ofMilliSatoshis(2_000),
            CHANNEL_ID_3,
            CHANNEL_ID,
            LocalDateTime.of(2021, 11, 29, 18, 30, 1, 500_000_000)
    );

    public static final ForwardingEvent FORWARDING_EVENT_3 = new ForwardingEvent(
            3,
            Coins.ofMilliSatoshis(30_000),
            Coins.ofMilliSatoshis(25_000),
            CHANNEL_ID_2,
            CHANNEL_ID_4,
            LocalDateTime.of(2021, 11, 29, 18, 30, 2)
    );

    public static final ForwardingEvent FORWARDING_EVENT_OLD = new ForwardingEvent(
            1,
            Coins.ofMilliSatoshis(1_000),
            Coins.ofMilliSatoshis(900),
            CHANNEL_ID,
            CHANNEL_ID_2,
            LocalDateTime.of(2000, 11, 29, 18, 30, 0, 500_000_000)
    );
}
