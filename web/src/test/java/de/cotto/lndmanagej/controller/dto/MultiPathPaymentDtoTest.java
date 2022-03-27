package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPathPaymentDtoTest {
    @Test
    void fromModel() {
        double probability = 0.999_995_238_095_464_9;
        String amountSat = "100";
        assertThat(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT)).isEqualTo(new MultiPathPaymentDto(
                amountSat,
                probability,
                String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis()),
                List.of(new RouteDto(amountSat, List.of(CHANNEL_ID), probability)))
        );
    }
}
