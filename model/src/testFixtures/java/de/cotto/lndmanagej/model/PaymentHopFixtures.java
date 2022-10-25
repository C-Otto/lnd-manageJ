package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;

public class PaymentHopFixtures {
    public static final PaymentHop PAYMENT_HOP_CHANNEL_1_FIRST =
            new PaymentHop(CHANNEL_ID, Coins.ofSatoshis(1), true);
    public static final PaymentHop PAYMENT_HOP_CHANNEL_1_LAST =
            new PaymentHop(CHANNEL_ID, Coins.ofSatoshis(1), false);
    public static final PaymentHop PAYMENT_HOP_CHANNEL_2_LAST =
            new PaymentHop(CHANNEL_ID_2, Coins.ofSatoshis(2), false);
    public static final PaymentHop PAYMENT_HOP_CHANNEL_3_FIRST =
            new PaymentHop(CHANNEL_ID_3, Coins.ofSatoshis(3), true);
    public static final PaymentHop PAYMENT_HOP_CHANNEL_4_FIRST =
            new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(4), true);
    public static final PaymentHop PAYMENT_HOP_CHANNEL_4_LAST =
            new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(4), false);
}
