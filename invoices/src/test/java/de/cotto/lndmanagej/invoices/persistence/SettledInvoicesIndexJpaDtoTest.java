package de.cotto.lndmanagej.invoices.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettledInvoicesIndexJpaDtoTest {
    @Test
    void id_initially_zero() {
        assertThat(new SettledInvoicesIndexJpaDto().getEntityId()).isEqualTo(0L);
    }

    @Test
    void offset_initially_zero() {
        assertThat(new SettledInvoicesIndexJpaDto().getAllSettledIndexOffset()).isEqualTo(0L);
    }

    @Test
    void set_and_get_id() {
        SettledInvoicesIndexJpaDto settledPaymentIndexJpaDto = new SettledInvoicesIndexJpaDto();
        settledPaymentIndexJpaDto.setEntityId(123);
        assertThat(settledPaymentIndexJpaDto.getEntityId()).isEqualTo(123L);
    }

    @Test
    void set_and_get_offset() {
        SettledInvoicesIndexJpaDto settledPaymentIndexJpaDto = new SettledInvoicesIndexJpaDto();
        settledPaymentIndexJpaDto.setAllSettledIndexOffset(42);
        assertThat(settledPaymentIndexJpaDto.getAllSettledIndexOffset()).isEqualTo(42L);
    }
}
