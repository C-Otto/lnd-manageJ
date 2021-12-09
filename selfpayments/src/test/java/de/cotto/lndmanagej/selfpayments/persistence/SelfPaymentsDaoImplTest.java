package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.SettledInvoice;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;
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
        when(repository.getSelfPaymentsToChannel(CHANNEL_ID.getShortChannelId()))
                .thenReturn(List.of(getDto(PAYMENT, SETTLED_INVOICE)));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel() {
        when(repository.getSelfPaymentsFromChannel(CHANNEL_ID.getShortChannelId()))
                .thenReturn(List.of(getDto(PAYMENT, SETTLED_INVOICE)));
        assertThat(selfPaymentsDaoImpl.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactlyInAnyOrder(SELF_PAYMENT);
    }

    private SelfPaymentJpaDto getDto(Payment payment, SettledInvoice settledInvoice) {
        return new SelfPaymentJpaDto(
                PaymentJpaDto.createFromModel(payment),
                SettledInvoiceJpaDto.createFromModel(settledInvoice)
        );
    }
}