package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.lndmanagej.model.FailureCode.PERMANENT_CHANNEL_FAILURE;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentInformationTest {
    private static final PaymentInformation PAYMENT_INFORMATION =
            new PaymentInformation(Coins.ofSatoshis(123), false, Optional.empty());

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
        assertThat(PAYMENT_INFORMATION.failureCode()).isEmpty();
    }

    @Test
    void withAdditionalInFlight() {
        assertThat(PAYMENT_INFORMATION.withAdditionalInFlight(Coins.ofSatoshis(77)))
                .isEqualTo(new PaymentInformation(Coins.ofSatoshis(200), false, Optional.empty()));
    }

    @Test
    void withIsSettled() {
        assertThat(PAYMENT_INFORMATION.withIsSettled())
                .isEqualTo(new PaymentInformation(Coins.ofSatoshis(123), true, Optional.empty()));
    }

    @Test
    void withIsFailed() {
        assertThat(PAYMENT_INFORMATION.withFailureCode(PERMANENT_CHANNEL_FAILURE)).isEqualTo(new PaymentInformation(
                Coins.ofSatoshis(123),
                false,
                Optional.of(PERMANENT_CHANNEL_FAILURE)
        ));
    }

    @Test
    void default_values() {
        assertThat(PaymentInformation.DEFAULT).isEqualTo(new PaymentInformation(Coins.NONE, false, Optional.empty()));
    }
}
