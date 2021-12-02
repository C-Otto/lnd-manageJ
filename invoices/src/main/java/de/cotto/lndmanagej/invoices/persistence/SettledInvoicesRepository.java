package de.cotto.lndmanagej.invoices.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettledInvoicesRepository extends JpaRepository<SettledInvoiceJpaDto, Long> {
    @Query("SELECT coalesce(max(i.addIndex), 0) FROM SettledInvoiceJpaDto i WHERE " +
            "i.settleIndex = (SELECT COUNT(j) FROM SettledInvoiceJpaDto j WHERE j.settleIndex <= i.settleIndex)")
    long getMaxAddIndexWithoutGaps();
}
