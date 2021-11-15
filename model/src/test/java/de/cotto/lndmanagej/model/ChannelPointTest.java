package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.OUTPUT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelPointTest {
    @Test
    void getTransactionHash() {
        assertThat(CHANNEL_POINT.getTransactionHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getIndex() {
        assertThat(CHANNEL_POINT.getOutput()).isEqualTo(OUTPUT);
    }

    @Test
    void getIndex_more_than_one_digit() {
        assertThat(ChannelPoint.create(TRANSACTION_HASH + ":123").getOutput()).isEqualTo(123);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ChannelPoint.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(CHANNEL_POINT).hasToString("abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0:1");
    }
}