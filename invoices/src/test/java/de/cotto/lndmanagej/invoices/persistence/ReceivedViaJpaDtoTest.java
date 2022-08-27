package de.cotto.lndmanagej.invoices.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReceivedViaJpaDtoTest {
    @Test
    void channelId() {
        ReceivedViaJpaDto instance = new ReceivedViaJpaDto();
        instance.setChannelId(123);
        assertThat(instance.getChannelId()).isEqualTo(123);
    }

    @Test
    void amount() {
        ReceivedViaJpaDto instance = new ReceivedViaJpaDto();
        instance.setAmount(456);
        assertThat(instance.getAmount()).isEqualTo(456);
    }
}
