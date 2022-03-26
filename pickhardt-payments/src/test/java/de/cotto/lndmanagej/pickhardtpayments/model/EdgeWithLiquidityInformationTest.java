package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformationFixtures.EDGE_WITH_LIQUIDITY_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;

class EdgeWithLiquidityInformationTest {
    @Test
    void edge() {
        assertThat(EDGE_WITH_LIQUIDITY_INFORMATION.edge()).isEqualTo(EDGE);
    }

    @Test
    void forKnownLiquidity() {
        Coins knownLiquidity = Coins.ofSatoshis(300);
        assertThat(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity))
                .isEqualTo(new EdgeWithLiquidityInformation(EDGE, knownLiquidity, knownLiquidity));
    }

    @Test
    void forUpperBound() {
        Coins upperBound = Coins.ofSatoshis(300);
        assertThat(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound))
                .isEqualTo(new EdgeWithLiquidityInformation(EDGE, Coins.NONE, upperBound));
    }

    @Test
    void availableLiquidityUpperBound() {
        assertThat(EDGE_WITH_LIQUIDITY_INFORMATION.availableLiquidityUpperBound()).isEqualTo(Coins.ofSatoshis(123));
    }

    @Test
    void availableLiquidityLowerBound() {
        assertThat(EDGE_WITH_LIQUIDITY_INFORMATION.availableLiquidityLowerBound()).isEqualTo(Coins.NONE);
    }
}
