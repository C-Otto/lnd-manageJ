package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;

public class PaymentHopFixtures {
    public static final PaymentHop PAYMENT_HOP_CHANNEL_1 = new PaymentHop(CHANNEL_ID, Coins.ofSatoshis(1));
    public static final PaymentHop PAYMENT_HOP_CHANNEL_2 = new PaymentHop(CHANNEL_ID_2, Coins.ofSatoshis(2));
    public static final PaymentHop PAYMENT_HOP_CHANNEL_3 = new PaymentHop(CHANNEL_ID_3, Coins.ofSatoshis(3));
    public static final PaymentHop PAYMENT_HOP_CHANNEL_4 = new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(4));
}
