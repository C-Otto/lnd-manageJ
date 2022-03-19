package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithCapacityInformationFixtures.EDGE_WITH_CAPACITY_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;

class EdgeWithCapacityInformationTest {
    @Test
    void edge() {
        assertThat(EDGE_WITH_CAPACITY_INFORMATION.edge()).isEqualTo(EDGE);
    }

    @Test
    void availableCapacity() {
        assertThat(EDGE_WITH_CAPACITY_INFORMATION.availableCapacity()).isEqualTo(Coins.ofSatoshis(123));
    }
}
