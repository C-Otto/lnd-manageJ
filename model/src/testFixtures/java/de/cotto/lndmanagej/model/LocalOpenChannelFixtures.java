package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_3;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.OpenInitiator.REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public class LocalOpenChannelFixtures {
    public static final long NUM_UPDATES = 100_200_300_400L;

    public static final Coins TOTAL_SENT = Coins.ofSatoshis(1_001);
    public static final Coins TOTAL_SENT_2 = Coins.ofSatoshis(101);
    public static final Coins TOTAL_RECEIVED = Coins.ofSatoshis(2_002);
    public static final Coins TOTAL_RECEIVED_2 = Coins.ofSatoshis(202);
    public static final Coins MIN_HTLC_CONSTRAINT = Coins.ofMilliSatoshis(2);

    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION,
                    LOCAL,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    false,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_MORE_BALANCE =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION_2,
                    LOCAL,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    false,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_PRIVATE =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION,
                    LOCAL,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    true,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_2 =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID_2, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION,
                    REMOTE,
                    TOTAL_SENT_2,
                    TOTAL_RECEIVED_2,
                    false,
                    false,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_MORE_BALANCE_2 =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID_2, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION_2,
                    REMOTE,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    false,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_3 =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID_3, CHANNEL_POINT, CAPACITY),
                    PUBKEY,
                    PUBKEY_2,
                    BALANCE_INFORMATION,
                    LOCAL,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    false,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_TO_NODE_3 =
            new LocalOpenChannel(
                    new ChannelCoreInformation(CHANNEL_ID_4, CHANNEL_POINT_3, CAPACITY_2),
                    PUBKEY,
                    PUBKEY_3,
                    BALANCE_INFORMATION,
                    LOCAL,
                    TOTAL_SENT,
                    TOTAL_RECEIVED,
                    false,
                    true,
                    NUM_UPDATES,
                    MIN_HTLC_CONSTRAINT
            );
}
