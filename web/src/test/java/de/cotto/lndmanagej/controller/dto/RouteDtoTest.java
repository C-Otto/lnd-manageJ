package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;

class RouteDtoTest {
    @Test
    void fromModel() {
        RouteDto expected = new RouteDto(
                "100",
                List.of(CHANNEL_ID, CHANNEL_ID_3, CHANNEL_ID_5),
                ROUTE.getProbability(),
                String.valueOf(ROUTE.fees().milliSatoshis()),
                String.valueOf(ROUTE.feesWithFirstHop().milliSatoshis()),
                ROUTE.getFeeRate(),
                ROUTE.getFeeRateWithFirstHop()
        );
        assertThat(RouteDto.fromModel(ROUTE))
                .isEqualTo(expected);
    }
}
