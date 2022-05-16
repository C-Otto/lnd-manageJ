package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;

public class RouteHintFixtures {
    public static final RouteHint ROUTE_HINT = new RouteHint(
            PUBKEY, PUBKEY_4, CHANNEL_ID, Coins.NONE, 123, 9
    );

    public static final RouteHint ROUTE_HINT_2 = new RouteHint(
            PUBKEY_3, PUBKEY_4, CHANNEL_ID_2, Coins.ofMilliSatoshis(1), 1234, 40
    );
}
