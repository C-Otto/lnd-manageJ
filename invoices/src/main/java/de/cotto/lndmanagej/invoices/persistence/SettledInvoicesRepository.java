package de.cotto.lndmanagej.invoices.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettledInvoicesRepository extends JpaRepository<SettledInvoiceJpaDto, Long> {
    @Query("SELECT coalesce(max(addIndex), 0) FROM SettledInvoiceJpaDto")
    long getMaxAddIndex();

    @Query("SELECT coalesce(max(i.settleIndex), 0) FROM SettledInvoiceJpaDto i WHERE i.settleIndex >= ?1 AND " +
            "i.settleIndex = (SELECT COUNT(j) FROM SettledInvoiceJpaDto j WHERE j.settleIndex <= i.settleIndex)")
    long getMaxSettledIndexWithoutGaps(long knownIndex);

    @Query("SELECT s FROM SettledInvoiceJpaDto s " +
            "JOIN s.receivedVia v " +
            "LEFT JOIN PaymentJpaDto p ON (s.hash = p.hash) " +
            "WHERE s.settleDate > ?2 AND v.channelId = ?1 AND p IS NULL")
    List<SettledInvoiceJpaDto> getInvoicesWithoutSelfPaymentsPaidVia(long channelId, long timestamp);
}
