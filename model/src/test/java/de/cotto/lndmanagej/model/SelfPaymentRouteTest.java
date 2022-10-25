package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

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
        PaymentHop firstHop = new PaymentHop(CHANNEL_ID_3, Coins.NONE, true);
        PaymentHop lastHop = new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(123), false);
        assertThat(SelfPaymentRoute.create(new PaymentRoute(firstHop, lastHop))).contains(expected);
    }

    @Test
    void create_no_hops() {
        assertThat(SelfPaymentRoute.create(new PaymentRoute(Optional.empty(), Optional.empty()))).isEmpty();
    }

    @Test
    void create_just_first_hop() {
        PaymentHop firstHop = new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(123), true);
        assertThat(SelfPaymentRoute.create(new PaymentRoute(Optional.of(firstHop), Optional.empty()))).isEmpty();
    }

    @Test
    void create_just_last_hop() {
        PaymentHop lastHop = new PaymentHop(CHANNEL_ID_4, Coins.ofSatoshis(123), false);
        assertThat(SelfPaymentRoute.create(new PaymentRoute(Optional.empty(), Optional.of(lastHop)))).isEmpty();
    }
}
