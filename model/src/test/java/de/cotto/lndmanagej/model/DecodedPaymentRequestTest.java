package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class DecodedPaymentRequestTest {
    @Test
    void description() {
        assertThat(DECODED_PAYMENT_REQUEST.description()).isEqualTo("description");
    }

    @Test
    void destination() {
        assertThat(DECODED_PAYMENT_REQUEST.destination()).isEqualTo(PUBKEY);
    }

    @Test
    void amount() {
        assertThat(DECODED_PAYMENT_REQUEST.amount()).isEqualTo(Coins.ofMilliSatoshis(123_456));
    }

    @Test
    void paymentRequest() {
        assertThat(DECODED_PAYMENT_REQUEST.paymentRequest()).isEqualTo("some payment request");
    }

    @Test
    void paymentHash() {
        assertThat(DECODED_PAYMENT_REQUEST.paymentHash()).isEqualTo(new HexString("FF00AB"));
    }

    @Test
    void paymentAddress() {
        assertThat(DECODED_PAYMENT_REQUEST.paymentAddress()).isEqualTo(new HexString("0011AABBCC"));
    }

    @Test
    void cltvExpiry() {
        assertThat(DECODED_PAYMENT_REQUEST.cltvExpiry()).isEqualTo(144);
    }

    @Test
    void creation() {
        Instant creationInstant = LocalDateTime.of(2022, 4, 24, 18, 45, 0).toInstant(ZoneOffset.UTC);
        assertThat(DECODED_PAYMENT_REQUEST.creation()).isEqualTo(creationInstant);
    }

    @Test
    void expiry() {
        Instant creationInstant = LocalDateTime.of(2023, 5, 25, 19, 46, 0).toInstant(ZoneOffset.UTC);
        assertThat(DECODED_PAYMENT_REQUEST.expiry()).isEqualTo(creationInstant);
    }
}
