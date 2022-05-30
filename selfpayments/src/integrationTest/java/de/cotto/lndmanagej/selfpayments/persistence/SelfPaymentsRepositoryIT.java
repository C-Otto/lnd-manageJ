package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.invoices.persistence.SettledInvoicesRepository;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import de.cotto.lndmanagej.payments.persistence.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_3;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_4;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_3;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_4;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_3;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_4;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SelfPaymentsRepositoryIT {
    @Autowired
    private SelfPaymentsRepository repository;

    @Autowired
    private SettledInvoicesRepository invoicesRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @BeforeEach
    void setUp() {
        paymentsRepository.save(PaymentJpaDto.createFromModel(PAYMENT));
        paymentsRepository.save(PaymentJpaDto.createFromModel(PAYMENT_2));
        paymentsRepository.save(PaymentJpaDto.createFromModel(PAYMENT_3));
        paymentsRepository.save(PaymentJpaDto.createFromModel(PAYMENT_4));
        invoicesRepository.save(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE));
        invoicesRepository.save(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_2));
        invoicesRepository.save(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_3));
        invoicesRepository.save(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_4));
    }

    @Test
    void getSelfPayments() {
        assertThat(repository.getAllSelfPayments()).map(SelfPaymentJpaDto::toModel)
                .containsExactlyInAnyOrder(SELF_PAYMENT, SELF_PAYMENT_2, SELF_PAYMENT_3, SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsToChannel() {
        assertThat(repository.getSelfPaymentsToChannel(CHANNEL_ID_2.getShortChannelId(), 0))
                .map(SelfPaymentJpaDto::toModel)
                .containsExactly(SELF_PAYMENT, SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsToChannel_with_max_age() {
        long minimumSettleDate = SETTLED_INVOICE_3.settleDate().toEpochSecond();
        assertThat(repository.getSelfPaymentsToChannel(CHANNEL_ID_2.getShortChannelId(), minimumSettleDate))
                .map(SelfPaymentJpaDto::toModel)
                .containsExactly(SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsToChannel_mpp_with_two_target_channels() {
        assertThat(repository.getSelfPaymentsToChannel(CHANNEL_ID.getShortChannelId(), 0))
                .map(SelfPaymentJpaDto::toModel)
                .contains(SELF_PAYMENT_4);
        assertThat(repository.getSelfPaymentsToChannel(CHANNEL_ID_2.getShortChannelId(), 0))
                .map(SelfPaymentJpaDto::toModel)
                .contains(SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsFromChannel() {
        assertThat(repository.getSelfPaymentsFromChannel(CHANNEL_ID_4.getShortChannelId(), 0))
            .map(SelfPaymentJpaDto::toModel)
            .containsExactlyInAnyOrder(SELF_PAYMENT, SELF_PAYMENT_2, SELF_PAYMENT_3, SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsFromChannel_with_max_age() {
        long minimumSettleDate = SELF_PAYMENT_3.settleDate().toEpochSecond();
        assertThat(repository.getSelfPaymentsFromChannel(CHANNEL_ID_4.getShortChannelId(), minimumSettleDate))
                .map(SelfPaymentJpaDto::toModel)
                .containsExactlyInAnyOrder(SELF_PAYMENT_3, SELF_PAYMENT_4);
    }

    @Test
    void getSelfPaymentsFromChannel_mpp_with_two_source_channels() {
        assertThat(repository.getSelfPaymentsFromChannel(CHANNEL_ID_4.getShortChannelId(), 0))
                .map(SelfPaymentJpaDto::toModel)
                .contains(SELF_PAYMENT_4);
        assertThat(repository.getSelfPaymentsFromChannel(CHANNEL_ID_3.getShortChannelId(), 0))
                .map(SelfPaymentJpaDto::toModel)
                .contains(SELF_PAYMENT_4);
    }
}
