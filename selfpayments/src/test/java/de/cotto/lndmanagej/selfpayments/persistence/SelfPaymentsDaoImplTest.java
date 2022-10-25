package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsDaoImplTest {
    private static final Duration MAX_AGE = Duration.ofDays(12);
    @InjectMocks
    private SelfPaymentsDaoImpl selfPaymentsDaoImpl;

    @Mock
    private SelfPaymentsRepository repository;

    private ZonedDateTime now;

    @BeforeEach
    void setUp() {
        now = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Test
    void getSelfPaymentsToChannel() {
        SelfPaymentJpaDto dto = getDto();
        when(repository.getSelfPaymentsToChannel(anyLong(), anyLong())).thenReturn(List.of(dto, dto));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsToChannel(CHANNEL_ID, MAX_AGE)).hasSize(1);
    }

    @Test
    void does_not_return_duplicates() {
        long epochSeconds = now.minus(MAX_AGE).toEpochSecond();
        when(repository.getSelfPaymentsToChannel(
                eq(CHANNEL_ID.getShortChannelId()),
                longThat(isWithinAFewSeconds(epochSeconds))
        )).thenReturn(List.of(getDto()));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsToChannel(CHANNEL_ID, MAX_AGE))
                .containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel() {
        long epochSeconds = now.minus(MAX_AGE).toEpochSecond();
        when(repository.getSelfPaymentsFromChannel(
                eq(CHANNEL_ID.getShortChannelId()),
                longThat(isWithinAFewSeconds(epochSeconds))
        )).thenReturn(List.of(getDto()));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsFromChannel(CHANNEL_ID, MAX_AGE))
                .containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private ArgumentMatcher<Long> isWithinAFewSeconds(long epochSeconds) {
        return value -> Math.abs(value - epochSeconds) < 10_000;
    }

    private SelfPaymentJpaDto getDto() {
        return new SelfPaymentJpaDto(
                SettledInvoiceJpaDto.createFromModel(de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE),
                PaymentJpaDto.createFromModel(de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT)
        );
    }
}
