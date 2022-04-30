package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPathPaymentDtoTest {
    @Test
    void fromModel() {
        double probability = 0.999_966_667_099_090_3;
        String amountSat1 = "100";
        String amountSat2 = "200";
        String amountSatSum = "300";
        String feesMilliSat = "40";
        RouteDto route1 = new RouteDto(
                amountSat1,
                List.of(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_5),
                0.999_985_714_354_421_7,
                feesMilliSat,
                "60",
                ROUTE.getFeeRate(),
                ROUTE.getFeeRateWithFirstHop()
        );
        RouteDto route2 = new RouteDto(
                amountSat2,
                List.of(CHANNEL_ID_2, CHANNEL_ID_3),
                0.999_980_952_472_562_4,
                feesMilliSat,
                "80",
                ROUTE_2.getFeeRate(),
                ROUTE_2.getFeeRateWithFirstHop()
        );
        assertThat(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT)).isEqualTo(new MultiPathPaymentDto(
                amountSatSum,
                probability,
                String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis()),
                String.valueOf(MULTI_PATH_PAYMENT.feesWithFirstHop().milliSatoshis()),
                MULTI_PATH_PAYMENT.getFeeRate(),
                MULTI_PATH_PAYMENT.getFeeRateWithFirstHop(),
                List.of(route1, route2))
        );
    }
}
