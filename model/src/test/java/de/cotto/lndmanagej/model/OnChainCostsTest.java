package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static org.assertj.core.api.Assertions.assertThat;

class OnChainCostsTest {
    @Test
    void add_none_to_none() {
        assertThat(OnChainCosts.NONE.add(OnChainCosts.NONE)).isEqualTo(OnChainCosts.NONE);
    }

    @Test
    void add() {
        assertThat(ON_CHAIN_COSTS.add(ON_CHAIN_COSTS)).isEqualTo(new OnChainCosts(
                Coins.ofSatoshis(2000),
                Coins.ofSatoshis(4000),
                Coins.ofSatoshis(6000)
        ));
    }

    @Test
    void open() {
        assertThat(ON_CHAIN_COSTS.open()).isEqualTo(Coins.ofSatoshis(1000));
    }

    @Test
    void close() {
        assertThat(ON_CHAIN_COSTS.close()).isEqualTo(Coins.ofSatoshis(2000));
    }

    @Test
    void sweep() {
        assertThat(ON_CHAIN_COSTS.sweep()).isEqualTo(Coins.ofSatoshis(3000));
    }
}