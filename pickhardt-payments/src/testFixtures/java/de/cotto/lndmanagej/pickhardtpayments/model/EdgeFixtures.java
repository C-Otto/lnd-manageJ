package de.cotto.lndmanagej.pickhardtpayments.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;

public class EdgeFixtures {
    public static final Edge EDGE = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);
    public static final Edge EDGE_1_3 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_3, CAPACITY, POLICY_1);
    public static final Edge EDGE_2_3 = new Edge(CHANNEL_ID_3, PUBKEY_2, PUBKEY_3, CAPACITY, POLICY_1);
    public static final Edge EDGE_3_2 = new Edge(CHANNEL_ID_4, PUBKEY_3, PUBKEY_2, CAPACITY_2, POLICY_1);
    public static final Edge EDGE_3_4 = new Edge(CHANNEL_ID_5, PUBKEY_3, PUBKEY_4, CAPACITY, POLICY_1);
}
