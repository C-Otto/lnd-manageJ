package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.SettledInvoice;
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
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsDaoImplTest {
    @InjectMocks
    private SelfPaymentsDaoImpl selfPaymentsDaoImpl;

    @Mock
    private SelfPaymentsRepository repository;

    @Test
    void getAllSelfPayments_empty() {
        assertThat(selfPaymentsDaoImpl.getAllSelfPayments()).isEmpty();
    }

    @Test
    void getAllSelfPayments() {
        when(repository.getAllSelfPayments())
                .thenReturn(List.of(getDto(PAYMENT, SETTLED_INVOICE), getDto(PAYMENT_2, SETTLED_INVOICE_2)));
        assertThat(selfPaymentsDaoImpl.getAllSelfPayments()).containsExactlyInAnyOrder(SELF_PAYMENT, SELF_PAYMENT_2);
    }

    @Test
    void getSelfPaymentsToChannel() {
        Duration maxAge = Duration.ofDays(12);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long epochSeconds = now.minus(maxAge).toEpochSecond();
        when(repository.getSelfPaymentsToChannel(
                eq(CHANNEL_ID.getShortChannelId()),
                longThat(isWithinAFewSeconds(epochSeconds))
        )).thenReturn(List.of(getDto(PAYMENT, SETTLED_INVOICE)));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsToChannel(CHANNEL_ID, maxAge))
                .containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel() {
        Duration maxAge = Duration.ofDays(13);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long epochSeconds = now.minus(maxAge).toEpochSecond();
        when(repository.getSelfPaymentsFromChannel(
                eq(CHANNEL_ID.getShortChannelId()),
                longThat(isWithinAFewSeconds(epochSeconds))
        )).thenReturn(List.of(getDto(PAYMENT, SETTLED_INVOICE)));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge))
                .containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private ArgumentMatcher<Long> isWithinAFewSeconds(long epochSeconds) {
        return value -> Math.abs(value - epochSeconds) < 10_000;
    }

    private SelfPaymentJpaDto getDto(Payment payment, SettledInvoice settledInvoice) {
        return new SelfPaymentJpaDto(
                settledInvoice.memo(),
                settledInvoice.settleDate().toEpochSecond(),
                settledInvoice.amountPaid().milliSatoshis(),
                payment.fees().milliSatoshis(),
                payment.getFirstChannel().map(ChannelId::getShortChannelId).orElse(0L),
                settledInvoice.receivedVia().map(ChannelId::getShortChannelId).orElse(0L)
        );
    }
}