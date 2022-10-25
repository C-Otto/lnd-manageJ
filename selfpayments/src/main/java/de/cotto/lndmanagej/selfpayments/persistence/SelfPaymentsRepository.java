package de.cotto.lndmanagej.selfpayments.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

public interface SelfPaymentsRepository extends JpaRepository<SelfPaymentsRepository.DummyEntity, Long> {
    @Query("SELECT NEW de.cotto.lndmanagej.selfpayments.persistence.SelfPaymentJpaDto(i, p) " +
            "FROM PaymentJpaDto p " +
            "JOIN SettledInvoiceJpaDto i ON p.hash = i.hash " +
            "JOIN p.routes route " +
            "JOIN route.hops hop " +
            "WHERE INDEX(hop) = SIZE(route.hops) - 1" +
            "AND hop.channelId = ?1 " +
            "AND i.settleDate >= ?2 " +
            "ORDER BY i.settleDate ASC")
    List<SelfPaymentJpaDto> getSelfPaymentsToChannel(long channelId, long minimumSettleDate);

    @Query("SELECT NEW de.cotto.lndmanagej.selfpayments.persistence.SelfPaymentJpaDto(i, p) " +
            "FROM PaymentJpaDto p " +
            "JOIN SettledInvoiceJpaDto i ON p.hash = i.hash " +
            "JOIN p.routes route " +
            "JOIN route.hops hop " +
            "WHERE INDEX(hop) = 0 " +
            "AND hop.channelId = ?1 " +
            "AND i.settleDate >= ?2 " +
            "ORDER BY i.settleDate ASC")
    List<SelfPaymentJpaDto> getSelfPaymentsFromChannel(long channelId, long minimumSettleDate);

    @Entity
    @Table(name = "dummy")
    class DummyEntity {
        @Id
        @SuppressWarnings("unused")
        private long dummyId;

        public DummyEntity() {
            // for JPA
        }
    }
}

