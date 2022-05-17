package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.Network.MAINNET;
import static de.cotto.lndmanagej.model.Network.REGTEST;
import static de.cotto.lndmanagej.model.Network.TESTNET;
import static org.assertj.core.api.Assertions.assertThat;

class NetworkTest {
    @Test
    void enumsExist() {
        assertThat(Network.values()).containsExactlyInAnyOrder(TESTNET, REGTEST, MAINNET);
    }
}
