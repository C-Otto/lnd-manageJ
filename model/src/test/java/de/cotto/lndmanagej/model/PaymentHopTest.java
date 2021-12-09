package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentHopTest {
    @Test
    void channelId() {
        assertThat(PAYMENT_HOP_CHANNEL_4.channelId()).isEqualTo(CHANNEL_ID_4);
    }

    @Test
    void amount() {
        assertThat(PAYMENT_HOP_CHANNEL_4.amount()).isEqualTo(Coins.ofSatoshis(4));
    }
}