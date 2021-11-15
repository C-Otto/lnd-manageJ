package de.cotto.lndmanagej.transactions.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionJpaDto, String> {
}
