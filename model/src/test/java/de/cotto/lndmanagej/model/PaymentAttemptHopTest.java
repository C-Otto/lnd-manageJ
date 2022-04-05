package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PaymentAttemptHopFixtures.PAYMENT_ATTEMPT_HOP;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentAttemptHopTest {

    @Test
    void channelId() {
        assertThat(PAYMENT_ATTEMPT_HOP.channelId()).contains(CHANNEL_ID);
    }

    @Test
    void amount() {
        assertThat(PAYMENT_ATTEMPT_HOP.amount()).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void targetPubkey() {
        assertThat(PAYMENT_ATTEMPT_HOP.targetPubkey()).contains(PUBKEY);
    }
}
