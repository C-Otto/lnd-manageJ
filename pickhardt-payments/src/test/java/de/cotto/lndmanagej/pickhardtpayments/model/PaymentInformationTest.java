package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentInformationTest {
    private static final PaymentInformation PAYMENT_INFORMATION =
            new PaymentInformation(Coins.ofSatoshis(123), false, false);

    @Test
    void inFlight() {
        assertThat(PAYMENT_INFORMATION.inFlight()).isEqualTo(Coins.ofSatoshis(123));
    }

    @Test
    void settled() {
        assertThat(PAYMENT_INFORMATION.settled()).isFalse();
    }

    @Test
    void failed() {
        assertThat(PAYMENT_INFORMATION.failed()).isFalse();
    }

    @Test
    void withAdditionalInFlight() {
        assertThat(PAYMENT_INFORMATION.withAdditionalInFlight(Coins.ofSatoshis(77)))
                .isEqualTo(new PaymentInformation(Coins.ofSatoshis(200), false, false));
    }

    @Test
    void withIsSettled() {
        assertThat(PAYMENT_INFORMATION.withIsSettled())
                .isEqualTo(new PaymentInformation(Coins.ofSatoshis(123), true, false));
    }

    @Test
    void withIsFailed() {
        assertThat(PAYMENT_INFORMATION.withIsFailed())
                .isEqualTo(new PaymentInformation(Coins.ofSatoshis(123), false, true));
    }

    @Test
    void default_values() {
        assertThat(PaymentInformation.DEFAULT).isEqualTo(new PaymentInformation(Coins.NONE, false, false));
    }
}
