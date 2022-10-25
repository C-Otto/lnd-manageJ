package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PaymentHop;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_1_FIRST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_FIRST;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentHopJpaDtoTest {
    @Test
    void toModel() {
        PaymentHopJpaDto dto =
                new PaymentHopJpaDto(CHANNEL_ID.getShortChannelId(), Coins.ofSatoshis(1).milliSatoshis(), true);
        assertThat(dto.toModel()).isEqualTo(PAYMENT_HOP_CHANNEL_1_FIRST);
    }

    @Test
    void createFromModel() {
        PaymentHop convertedTwice = PaymentHopJpaDto.createFromModel(PAYMENT_HOP_CHANNEL_4_FIRST).toModel();
        assertThat(convertedTwice).isEqualTo(PAYMENT_HOP_CHANNEL_4_FIRST);
    }
}
