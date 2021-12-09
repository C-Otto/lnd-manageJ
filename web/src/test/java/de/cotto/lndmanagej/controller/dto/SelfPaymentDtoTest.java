package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentDtoTest {

    private final SelfPaymentDto dto = SelfPaymentDto.createFromModel(SELF_PAYMENT);

    @Test
    void settleDate() {
        assertThat(dto.settleDate()).isEqualTo(SELF_PAYMENT.settleDate());
    }

    @Test
    void memo() {
        assertThat(dto.memo()).isEqualTo(SELF_PAYMENT.memo());
    }

    @Test
    void value() {
        assertThat(dto.value()).isEqualTo(String.valueOf(SELF_PAYMENT.value().milliSatoshis()));
    }

    @Test
    void fees() {
        assertThat(dto.fees()).isEqualTo(String.valueOf(SELF_PAYMENT.fees().milliSatoshis()));
    }

    @Test
    void firstChannel() {
        assertThat(dto.firstChannel()).isEqualTo(SELF_PAYMENT.firstChannel().orElseThrow());
    }

    @Test
    void lastChannel() {
        assertThat(dto.lastChannel()).isEqualTo(SELF_PAYMENT.lastChannel().orElseThrow());
    }
}