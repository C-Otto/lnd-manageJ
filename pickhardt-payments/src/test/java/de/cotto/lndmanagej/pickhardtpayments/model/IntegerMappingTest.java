package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class IntegerMappingTest {
    private final IntegerMapping<Pubkey> integerMapping = new IntegerMapping<>();

    @Test
    void unknown() {
        assertThat(integerMapping.getMappedInteger(PUBKEY)).isZero();
    }

    @Test
    void known() {
        integerMapping.getMappedInteger(PUBKEY);
        assertThat(integerMapping.getMappedInteger(PUBKEY)).isZero();
        assertThat(integerMapping.getKey(0)).isEqualTo(PUBKEY);
    }

    @Test
    void second_node() {
        integerMapping.getMappedInteger(PUBKEY);
        assertThat(integerMapping.getMappedInteger(PUBKEY_2)).isOne();
        assertThat(integerMapping.getKey(1)).isEqualTo(PUBKEY_2);
    }

    @Test
    void second_node_known() {
        integerMapping.getMappedInteger(PUBKEY);
        integerMapping.getMappedInteger(PUBKEY_2);
        assertThat(integerMapping.getMappedInteger(PUBKEY_2)).isOne();
        assertThat(integerMapping.getKey(1)).isEqualTo(PUBKEY_2);
    }

    @Test
    void two_nodes() {
        integerMapping.getMappedInteger(PUBKEY);
        integerMapping.getMappedInteger(PUBKEY_2);
        assertThat(integerMapping.getMappedInteger(PUBKEY)).isZero();
        assertThat(integerMapping.getMappedInteger(PUBKEY_2)).isOne();
    }

    @Test
    void two_nodes_getKey() {
        integerMapping.getMappedInteger(PUBKEY);
        integerMapping.getMappedInteger(PUBKEY_2);
        assertThat(integerMapping.getKey(0)).isEqualTo(PUBKEY);
        assertThat(integerMapping.getKey(1)).isEqualTo(PUBKEY_2);
    }
}