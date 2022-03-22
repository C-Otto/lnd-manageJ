package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;

class RouteDtoTest {
    @Test
    void fromModel() {
        assertThat(RouteDto.fromModel(ROUTE))
                .isEqualTo(new RouteDto("100", List.of(CHANNEL_ID), ROUTE.getProbability()));
    }
}
