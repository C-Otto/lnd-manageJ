package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentsRepositoryIT {
    @Autowired
    private PaymentsRepository repository;

    @Test
    void getMaxIndex_no_payment() {
        assertThat(repository.getMaxIndex()).isEqualTo(0);
    }

    @Test
    void getMaxIndex() {
        repository.save(payment(1));
        repository.save(payment(2));
        repository.save(payment(3));
        assertThat(repository.getMaxIndex()).isEqualTo(3L);
    }

    private PaymentJpaDto payment(int index) {
        return PaymentJpaDto.createFromModel(
                new Payment(index, PAYMENT.paymentHash(), PAYMENT.creationDateTime(), PAYMENT.value(), PAYMENT.fees())
        );
    }
}