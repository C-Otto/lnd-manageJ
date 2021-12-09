package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_1;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentHopJpaDtoTest {
    @Test
    void toModel() {
        assertThat(new PaymentHopJpaDto(CHANNEL_ID.getShortChannelId(), Coins.ofSatoshis(1).milliSatoshis()).toModel())
                .isEqualTo(PAYMENT_HOP_CHANNEL_1);
    }

    @Test
    void createFromModel() {
        assertThat(PaymentHopJpaDto.createFromModel(PAYMENT_HOP_CHANNEL_4).toModel()).isEqualTo(PAYMENT_HOP_CHANNEL_4);
    }
}