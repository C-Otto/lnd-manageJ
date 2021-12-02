package de.cotto.lndmanagej.invoices.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static org.assertj.core.api.Assertions.assertThat;

class SettledSettledInvoiceJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void createFromInvoice() {
        SettledInvoiceJpaDto jpaDto = SettledInvoiceJpaDto.createFromInvoice(SETTLED_INVOICE);
        assertThat(jpaDto.getAddIndex()).isEqualTo(SETTLED_INVOICE.addIndex());
        assertThat(jpaDto.getSettleIndex()).isEqualTo(SETTLED_INVOICE.settleIndex());
        assertThat(jpaDto.getSettleDate())
                .isEqualTo(SETTLED_INVOICE.settleDate().toEpochSecond(ZoneOffset.UTC));
        assertThat(jpaDto.getHash()).isEqualTo(SETTLED_INVOICE.hash());
        assertThat(jpaDto.getAmountPaid()).isEqualTo(SETTLED_INVOICE.amountPaid().milliSatoshis());
        assertThat(jpaDto.getMemo()).isEqualTo(SETTLED_INVOICE.memo());
    }

    @Test
    void toModel() {
        assertThat(SettledInvoiceJpaDto.createFromInvoice(SETTLED_INVOICE).toModel())
                .isEqualTo(SETTLED_INVOICE);
    }
}

