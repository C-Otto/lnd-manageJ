package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentJpaDtoTest {
    private SelfPaymentJpaDto selfPaymentJpaDto;

    @BeforeEach
    void setUp() {
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SETTLED_INVOICE.memo(),
                SETTLED_INVOICE.settleDate().toEpochSecond(),
                SETTLED_INVOICE.amountPaid().milliSatoshis(),
                PAYMENT.fees().milliSatoshis(),
                PAYMENT.getFirstChannel().map(ChannelId::getShortChannelId).orElse(0L),
                SETTLED_INVOICE.receivedVia().map(ChannelId::getShortChannelId).orElse(0L)
        );
    }

    @Test
    void toModel() {
        assertThat(selfPaymentJpaDto.toModel()).isEqualTo(SELF_PAYMENT);
    }

    @Test
    void toModel_no_first_channel() {
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SETTLED_INVOICE_2.memo(),
                SETTLED_INVOICE_2.settleDate().toEpochSecond(),
                SETTLED_INVOICE_2.amountPaid().milliSatoshis(),
                PAYMENT_2.fees().milliSatoshis(),
                0L,
                SETTLED_INVOICE.receivedVia().map(ChannelId::getShortChannelId).orElse(0L)
        );
        assertThat(selfPaymentJpaDto.toModel().firstChannel()).isEmpty();
    }

    @Test
    void toModel_no_received_via() {
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SETTLED_INVOICE_2.memo(),
                SETTLED_INVOICE_2.settleDate().toEpochSecond(),
                SETTLED_INVOICE_2.amountPaid().milliSatoshis(),
                PAYMENT_2.fees().milliSatoshis(),
                PAYMENT_2.getFirstChannel().map(ChannelId::getShortChannelId).orElse(0L),
                0L
        );
        assertThat(selfPaymentJpaDto.toModel().lastChannel()).isEmpty();
    }

    @Test
    void memo() {
        assertThat(selfPaymentJpaDto.memo()).isEqualTo(SETTLED_INVOICE.memo());
    }

    @Test
    void settleDate() {
        assertThat(selfPaymentJpaDto.settleDate()).isEqualTo(SETTLED_INVOICE.settleDate().toEpochSecond());
    }

    @Test
    void amountPaid() {
        assertThat(selfPaymentJpaDto.amountPaid()).isEqualTo(SETTLED_INVOICE.amountPaid().milliSatoshis());
    }

    @Test
    void fees() {
        assertThat(selfPaymentJpaDto.fees()).isEqualTo(PAYMENT.fees().milliSatoshis());
    }

    @Test
    void firstChannel() {
        assertThat(selfPaymentJpaDto.firstChannel())
                .isEqualTo(PAYMENT.getFirstChannel().map(ChannelId::getShortChannelId).orElseThrow());
    }

    @Test
    void receivedVia() {
        assertThat(selfPaymentJpaDto.receivedVia())
                .isEqualTo(SETTLED_INVOICE.receivedVia().map(ChannelId::getShortChannelId).orElseThrow());
    }
}