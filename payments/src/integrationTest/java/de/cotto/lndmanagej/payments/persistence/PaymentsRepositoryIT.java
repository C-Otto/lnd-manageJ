package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentsRepositoryIT {
    @Autowired
    private PaymentsRepository repository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    void no_stale_route_data_when_adding_already_existing_payment() {
        PaymentJpaDto payment = payment(1);
        repository.save(payment);
        int initial = countPersistedRoutes();
        repository.save(payment);
        assertThat(countPersistedRoutes()).isEqualTo(initial);
    }

    @Test
    void no_stale_hop_data_when_adding_already_existing_payment() {
        PaymentJpaDto payment = payment(1);
        repository.save(payment);
        int initial = countPersistedHops();
        repository.save(payment);
        assertThat(countPersistedHops()).isEqualTo(initial);
    }

    private int countPersistedRoutes() {
        return countEntries("PAYMENT_ROUTES");
    }

    private int countPersistedHops() {
        return countEntries("PAYMENT_ROUTE_HOPS");
    }

    private int countEntries(String table) {
        Number singleResult = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table).getSingleResult();
        return singleResult.intValue();
    }

    private PaymentJpaDto payment(int index) {
        return PaymentJpaDto.createFromModel(new Payment(
                        index,
                        PAYMENT.paymentHash(),
                        PAYMENT.creationDateTime(),
                        PAYMENT.value(),
                        PAYMENT.fees(),
                        PAYMENT.routes()
                )
        );
    }
}
