package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

class ForwardingEventJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void createFromForwardingEvent() {
        ForwardingEventJpaDto jpaDto = ForwardingEventJpaDto.createFromForwardingEvent(FORWARDING_EVENT);
        assertThat(jpaDto.getIndex()).isEqualTo(FORWARDING_EVENT.index());
        assertThat(jpaDto.getAmountIn()).isEqualTo(FORWARDING_EVENT.amountIn().milliSatoshis());
        assertThat(jpaDto.getAmountOut()).isEqualTo(FORWARDING_EVENT.amountOut().milliSatoshis());
        assertThat(jpaDto.getChannelIn()).isEqualTo(FORWARDING_EVENT.channelIn().getShortChannelId());
        assertThat(jpaDto.getChannelOut()).isEqualTo(FORWARDING_EVENT.channelOut().getShortChannelId());
        assertThat(jpaDto.getTimestamp())
                .isEqualTo(FORWARDING_EVENT.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
    }
}