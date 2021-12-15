package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OffChainCostsFixtures.OFF_CHAIN_COSTS;
import static org.assertj.core.api.Assertions.assertThat;

class OffChainCostsTest {
    @Test
    void none() {
        assertThat(OffChainCosts.NONE).isEqualTo(new OffChainCosts(Coins.NONE, Coins.NONE));
    }

    @Test
    void add_none_to_none() {
        assertThat(OffChainCosts.NONE.add(OffChainCosts.NONE)).isEqualTo(OffChainCosts.NONE);
    }

    @Test
    void add() {
        assertThat(OFF_CHAIN_COSTS.add(OFF_CHAIN_COSTS)).isEqualTo(new OffChainCosts(
                Coins.ofSatoshis(2000),
                Coins.ofSatoshis(4000)
        ));
    }

    @Test
    void rebalanceSourceCosts() {
        assertThat(OFF_CHAIN_COSTS.rebalanceSource()).isEqualTo(Coins.ofSatoshis(1000));
    }

    @Test
    void rebalanceTargetCosts() {
        assertThat(OFF_CHAIN_COSTS.rebalanceTarget()).isEqualTo(Coins.ofSatoshis(2000));
    }
}