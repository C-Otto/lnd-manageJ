package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_FIRST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_LAST;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentHopTest {
    @Test
    void channelId() {
        assertThat(PAYMENT_HOP_CHANNEL_4_FIRST.channelId()).isEqualTo(CHANNEL_ID_4);
    }

    @Test
    void amount() {
        assertThat(PAYMENT_HOP_CHANNEL_4_FIRST.amount()).isEqualTo(Coins.ofSatoshis(4));
    }

    @Test
    void first() {
        assertThat(PAYMENT_HOP_CHANNEL_4_LAST.first()).isFalse();
        assertThat(PAYMENT_HOP_CHANNEL_4_FIRST.first()).isTrue();
    }

    @Test
    void last() {
        assertThat(PAYMENT_HOP_CHANNEL_4_LAST.last()).isTrue();
        assertThat(PAYMENT_HOP_CHANNEL_4_FIRST.last()).isFalse();
    }
}
