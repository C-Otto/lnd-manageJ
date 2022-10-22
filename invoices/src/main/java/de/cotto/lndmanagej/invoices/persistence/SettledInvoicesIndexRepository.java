package de.cotto.lndmanagej.invoices.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettledInvoicesIndexRepository extends JpaRepository<SettledInvoicesIndexJpaDto, Long> {
    Optional<SettledInvoicesIndexJpaDto> findByEntityId(long entityId);
}
