package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentHopTest {
    @Test
    void channelId() {
        assertThat(PAYMENT_HOP.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void amount() {
        assertThat(PAYMENT_HOP.amount()).isEqualTo(Coins.ofSatoshis(1));
    }
}