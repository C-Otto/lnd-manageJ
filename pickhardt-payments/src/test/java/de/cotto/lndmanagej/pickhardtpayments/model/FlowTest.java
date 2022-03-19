package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FlowTest {
    @Test
    void amount_must_not_be_zero() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Flow(EDGE, Coins.NONE)
        ).withMessage("Amount must be positive");
    }

    @Test
    void amount_must_not_be_negative() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Flow(EDGE, Coins.ofSatoshis(-1))
        ).withMessage("Amount must be positive");
    }

    @Test
    void source_and_target_must_be_different() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Flow(new Edge(CHANNEL_ID, PUBKEY, PUBKEY, CAPACITY), Coins.ofSatoshis(1))
        ).withMessage("Source and target must be different");
    }

    @Test
    void edge() {
        assertThat(FLOW.edge()).isEqualTo(EDGE);
    }

    @Test
    void amount() {
        assertThat(FLOW.amount()).isEqualTo(Coins.ofSatoshis(1));
    }

    @Test
    void getProbability() {
        Coins capacitySat = FLOW.edge().capacity();
        long flowSat = FLOW.amount().satoshis();
        assertThat(FLOW.getProbability())
                .isEqualTo(1.0 * (capacitySat.satoshis() + 1 - flowSat) / (capacitySat.satoshis() + 1));
    }
}
