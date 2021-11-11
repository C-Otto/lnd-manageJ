package de.cotto.lndmanagej.controller;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class PubkeyConverterTest {
    @Test
    void convert() {
        assertThat(new PubkeyConverter().convert(PUBKEY.toString())).isEqualTo(PUBKEY);
    }
}