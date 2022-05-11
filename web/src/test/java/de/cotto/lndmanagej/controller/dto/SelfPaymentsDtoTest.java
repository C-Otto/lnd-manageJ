package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentsDtoTest {
    @Test
    void no_self_payments() {
        SelfPaymentsDto empty = new SelfPaymentsDto(List.of());
        assertThat(empty.selfPayments()).isEmpty();
    }

    @Test
    void amountPaid() {
        SelfPaymentsDto empty = new SelfPaymentsDto(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        String expected = String.valueOf(SELF_PAYMENT.amountPaid().add(SELF_PAYMENT_2.amountPaid()).milliSatoshis());
        assertThat(empty.amountPaidMilliSat()).isEqualTo(expected);
    }

    @Test
    void fees() {
        SelfPaymentsDto empty = new SelfPaymentsDto(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        String expected = String.valueOf(SELF_PAYMENT.fees().add(SELF_PAYMENT_2.fees()).milliSatoshis());
        assertThat(empty.feesMilliSat()).isEqualTo(expected);
    }
}