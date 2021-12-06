package de.cotto.lndmanagej.payments.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_CREATION_DATE_TIME;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_FEES;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_HASH;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void createFromModel() {
        PaymentJpaDto jpaDto = PaymentJpaDto.createFromModel(PAYMENT);
        assertThat(jpaDto.getPaymentIndex()).isEqualTo(PAYMENT_INDEX);
        assertThat(jpaDto.getHash()).isEqualTo(PAYMENT_HASH);
        assertThat(jpaDto.getTimestamp())
                .isEqualTo(PAYMENT_CREATION_DATE_TIME.toInstant(ZoneOffset.UTC).toEpochMilli());
        assertThat(jpaDto.getValue()).isEqualTo(PAYMENT_VALUE.milliSatoshis());
        assertThat(jpaDto.getFees()).isEqualTo(PAYMENT_FEES.milliSatoshis());
        assertThat(jpaDto.getRoutes()).hasSize(1);
    }

    @Test
    void toModel() {
        assertThat(PaymentJpaDto.createFromModel(PAYMENT).toModel()).isEqualTo(PAYMENT);
    }
}

