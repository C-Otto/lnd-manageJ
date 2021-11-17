package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public  class CoopClosedChannelFixtures {
    public static final CoopClosedChannel CLOSED_CHANNEL =
            new CoopClosedChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2);
    public static final CoopClosedChannel CLOSED_CHANNEL_2
            = new CoopClosedChannel(CHANNEL_ID_2, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2);
    public static final CoopClosedChannel CLOSED_CHANNEL_3 =
            new CoopClosedChannel(CHANNEL_ID_3, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2);
    public static final CoopClosedChannel CLOSED_CHANNEL_TO_NODE_3 =
            new CoopClosedChannel(CHANNEL_ID_3, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_3, TRANSACTION_HASH_2);
}
