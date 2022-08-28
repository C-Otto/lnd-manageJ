package de.cotto.lndmanagej.payments.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettledPaymentIndexJpaDtoTest {
    @Test
    void id_initially_zero() {
        assertThat(new SettledPaymentIndexJpaDto().getEntityId()).isEqualTo(0L);
    }

    @Test
    void offset_initially_zero() {
        assertThat(new SettledPaymentIndexJpaDto().getAllSettledIndexOffset()).isEqualTo(0L);
    }

    @Test
    void set_and_get_id() {
        SettledPaymentIndexJpaDto settledPaymentIndexJpaDto = new SettledPaymentIndexJpaDto();
        settledPaymentIndexJpaDto.setEntityId(123);
        assertThat(settledPaymentIndexJpaDto.getEntityId()).isEqualTo(123L);
    }

    @Test
    void set_and_get_offset() {
        SettledPaymentIndexJpaDto settledPaymentIndexJpaDto = new SettledPaymentIndexJpaDto();
        settledPaymentIndexJpaDto.setAllSettledIndexOffset(42);
        assertThat(settledPaymentIndexJpaDto.getAllSettledIndexOffset()).isEqualTo(42L);
    }
}
