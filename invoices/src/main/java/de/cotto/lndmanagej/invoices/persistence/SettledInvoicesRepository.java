package de.cotto.lndmanagej.invoices.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettledInvoicesRepository extends JpaRepository<SettledInvoiceJpaDto, Long> {
    @Query("SELECT coalesce(max(addIndex), 0) FROM SettledInvoiceJpaDto")
    long getMaxAddIndex();

    @Query("SELECT coalesce(max(i.settleIndex), 0) FROM SettledInvoiceJpaDto i WHERE " +
            "i.settleIndex = (SELECT COUNT(j) FROM SettledInvoiceJpaDto j WHERE j.settleIndex <= i.settleIndex)")
    long getMaxSettledIndexWithoutGaps();

    List<SettledInvoiceJpaDto> findAllByReceivedViaChannelIdAndSettleDateAfter(long channelId, long timestamp);
}
