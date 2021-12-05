package de.cotto.lndmanagej.payments.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentsRepository extends JpaRepository<PaymentJpaDto, Long> {
    @Query("SELECT coalesce(max(paymentIndex), 0) FROM PaymentJpaDto")
    long getMaxIndex();
}
