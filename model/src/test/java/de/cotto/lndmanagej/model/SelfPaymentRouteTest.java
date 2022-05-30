package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentRouteTest {
    private static final SelfPaymentRoute SELF_PAYMENT_HTLC =
            new SelfPaymentRoute(CHANNEL_ID, Coins.ofSatoshis(1), CHANNEL_ID_2);

    @Test
    void amount() {
        assertThat(SELF_PAYMENT_HTLC.amount()).isEqualTo(Coins.ofSatoshis(1));
    }

    @Test
    void channelIdOut() {
        assertThat(SELF_PAYMENT_HTLC.channelIdOut()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void channelIdIn() {
        assertThat(SELF_PAYMENT_HTLC.channelIdIn()).isEqualTo(CHANNEL_ID_2);
    }

    @Test
    void create() {
        SelfPaymentRoute expected = new SelfPaymentRoute(CHANNEL_ID_3, Coins.ofSatoshis(123), CHANNEL_ID_4);
        PaymentHop hop1 = new PaymentHop(CHANNEL_ID_3, Coins.NONE);
        PaymentHop hop2 = new PaymentHop(CHANNEL_ID, Coins.NONE);
        PaymentHop hop3 = new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(123));
        assertThat(SelfPaymentRoute.create(new PaymentRoute(List.of(hop1, hop2, hop3)))).contains(expected);
    }

    @Test
    void create_no_hops() {
        assertThat(SelfPaymentRoute.create(new PaymentRoute(List.of()))).isEmpty();
    }
}
