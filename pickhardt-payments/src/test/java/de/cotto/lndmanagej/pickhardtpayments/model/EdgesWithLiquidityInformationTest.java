package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_1_3;
import static org.assertj.core.api.Assertions.assertThat;

class EdgesWithLiquidityInformationTest {
    @Test
    void edges_empty() {
        assertThat(EdgesWithLiquidityInformation.EMPTY.edges()).isEmpty();
    }

    @Test
    void edges() {
        EdgeWithLiquidityInformation edge1 = EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(1));
        EdgeWithLiquidityInformation edge2 = EdgeWithLiquidityInformation.forLowerBound(EDGE_1_3, Coins.ofSatoshis(10));
        assertThat(new EdgesWithLiquidityInformation(edge1, edge2).edges()).containsExactly(edge1, edge2);
    }
}
