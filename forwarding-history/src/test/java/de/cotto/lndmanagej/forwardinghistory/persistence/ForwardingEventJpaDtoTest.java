package de.cotto.lndmanagej.forwardinghistory.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

class ForwardingEventJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void createFromModel() {
        ForwardingEventJpaDto jpaDto = ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT);
        assertThat(jpaDto.getIndex()).isEqualTo(FORWARDING_EVENT.index());
        assertThat(jpaDto.getAmountIncoming()).isEqualTo(FORWARDING_EVENT.amountIn().milliSatoshis());
        assertThat(jpaDto.getAmountOutgoing()).isEqualTo(FORWARDING_EVENT.amountOut().milliSatoshis());
        assertThat(jpaDto.getChannelIncoming()).isEqualTo(FORWARDING_EVENT.channelIn().getShortChannelId());
        assertThat(jpaDto.getChannelOutgoing()).isEqualTo(FORWARDING_EVENT.channelOut().getShortChannelId());
        assertThat(jpaDto.getTimestamp())
                .isEqualTo(FORWARDING_EVENT.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Test
    void toModel() {
        assertThat(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT).toModel())
                .isEqualTo(FORWARDING_EVENT);
    }
}