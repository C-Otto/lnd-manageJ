package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.invoices.SettledInvoicesDao;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.AMOUNT_PAID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettledInvoicesServiceTest {
    private static final Duration MAX_AGE = Duration.ofMinutes(456);
    @InjectMocks
    private SettledInvoicesService settledInvoicesService;

    @Mock
    private SettledInvoicesDao dao;

    @Test
    void getAmountReceivedViaChannelWithoutSelfPayments_no_settled_invoices() {
        assertThat(settledInvoicesService.getAmountReceivedViaChannelWithoutSelfPayments(CHANNEL_ID, MAX_AGE))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void getAmountReceivedViaChannelWithoutSelfPayments() {
        Coins amountReceivedPerInvoice = Coins.ofMilliSatoshis(CHANNEL_ID.getShortChannelId());
        when(dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, MAX_AGE)).thenReturn(List.of(SETTLED_INVOICE));
        assertThat(settledInvoicesService.getAmountReceivedViaChannelWithoutSelfPayments(CHANNEL_ID, MAX_AGE))
                .isEqualTo(amountReceivedPerInvoice);
    }

    @Test
    void getAmountReceivedViaChannelWithoutSelfPayments_two_invoices() {
        Coins amountReceivedPerInvoice = Coins.ofMilliSatoshis(CHANNEL_ID.getShortChannelId());
        Coins expected = amountReceivedPerInvoice.add(amountReceivedPerInvoice);
        when(dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, MAX_AGE))
                .thenReturn(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
        assertThat(settledInvoicesService.getAmountReceivedViaChannelWithoutSelfPayments(CHANNEL_ID, MAX_AGE))
                .isEqualTo(expected);
    }

    @Test
    void getAmountReceivedViaChannelWithoutSelfPayments_invoice_paid_via_two_channels() {
        Coins oneMilliSatoshi = Coins.ofMilliSatoshis(1);
        SettledInvoice invoice = new SettledInvoice(
                SETTLED_INVOICE.addIndex(),
                SETTLED_INVOICE.settleIndex(),
                SETTLED_INVOICE.settleDate(),
                SETTLED_INVOICE.hash(),
                SETTLED_INVOICE.amountPaid(),
                SETTLED_INVOICE.memo(),
                SETTLED_INVOICE.keysendMessage(),
                Map.of(CHANNEL_ID, oneMilliSatoshi, CHANNEL_ID_2, AMOUNT_PAID.subtract(oneMilliSatoshi))
        );
        when(dao.getInvoicesWithoutSelfPaymentsPaidVia(CHANNEL_ID, MAX_AGE)).thenReturn(List.of(invoice));
        assertThat(settledInvoicesService.getAmountReceivedViaChannelWithoutSelfPayments(CHANNEL_ID, MAX_AGE))
                .isEqualTo(oneMilliSatoshi);
    }
}
