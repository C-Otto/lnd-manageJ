package de.cotto.lndmanagej.payments.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettledPaymentIndexRepository extends JpaRepository<SettledPaymentIndexJpaDto, Long> {
    Optional<SettledPaymentIndexJpaDto> findByEntityId(long entityId);
}
