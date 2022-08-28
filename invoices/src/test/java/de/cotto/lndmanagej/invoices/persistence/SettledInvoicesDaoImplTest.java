package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettledInvoicesDaoImplTest {
    private static final Duration MAX_AGE = Duration.ofDays(10 * 365);

    @InjectMocks
    private SettledInvoicesDaoImpl dao;

    @Mock
    private SettledInvoicesRepository repository;

    @Test
    void getSettleIndexOffset_initially_0() {
        when(repository.getMaxSettledIndexWithoutGaps()).thenReturn(0L);
        assertThat(dao.getSettleIndexOffset()).isEqualTo(0);
    }

    @Test
    void getSettleIndexOffset() {
        long expectedOffset = 123;
        when(repository.getMaxSettledIndexWithoutGaps()).thenReturn(expectedOffset);
        assertThat(dao.getSettleIndexOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void getAddIndexOffset_initially_0() {
        when(repository.getMaxAddIndex()).thenReturn(0L);
        assertThat(dao.getAddIndexOffset()).isEqualTo(0);
    }

    @Test
    void getMaxAddIndexWithoutGaps() {
        long expectedOffset = 123;
        when(repository.getMaxAddIndex()).thenReturn(expectedOffset);
        assertThat(dao.getAddIndexOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void save_single() {
        dao.save(SETTLED_INVOICE);
        verify(repository).save(argThat(jpaDto -> jpaDto.getMemo().equals(SETTLED_INVOICE.memo())));
    }

    @Test
    void save_empty() {
        dao.save(Set.of());
        verify(repository).saveAll(List.of());
    }

    @Test
    void save_two() {
        dao.save(Set.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
        Set<SettledInvoice> expected = Set.of(SETTLED_INVOICE, SETTLED_INVOICE_2);
        verify(repository).saveAll(argThat(isSet(expected)));
    }

    @Test
    void getInvoicesPaidVia_empty() {
        assertThat(dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, Duration.ofMinutes(1))).isEmpty();
    }

    @Test
    void getInvoicesPaidVia() {
        SettledInvoiceJpaDto jpaDto = SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE);
        when(repository.getInvoicesWithoutSelfPaymentsPaidVia(eq(CHANNEL_ID.getShortChannelId()), anyLong()))
                .thenReturn(List.of(jpaDto));
        assertThat(dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, MAX_AGE)).containsExactly(SETTLED_INVOICE);
    }

    @Test
    void getInvoicesPaidVia_with_max_age_in_seconds() {
        Duration maxAge = Duration.ofDays(17);
        long expectedTimestamp = Instant.now().minus(maxAge).getEpochSecond();
        dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, maxAge);
        verify(repository).getInvoicesWithoutSelfPaymentsPaidVia(
                anyLong(),
                longThat(timestamp -> Math.abs(expectedTimestamp - timestamp) <= 5)
        );
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private <S extends SettledInvoiceJpaDto> ArgumentMatcher<Iterable<S>> isSet(Set<SettledInvoice> expected) {
        return iterable -> iterable instanceof List && ((List<S>) iterable).stream()
                .map(SettledInvoiceJpaDto::toModel)
                .collect(Collectors.toSet())
                .equals(expected);
    }
}
