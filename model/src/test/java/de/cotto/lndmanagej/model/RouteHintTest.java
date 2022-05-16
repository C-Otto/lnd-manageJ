package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.model.RouteHintFixtures.ROUTE_HINT;
import static org.assertj.core.api.Assertions.assertThat;

class RouteHintTest {
    @Test
    void sourceNode() {
        assertThat(ROUTE_HINT.sourceNode()).isEqualTo(PUBKEY);
    }

    @Test
    void endNode() {
        assertThat(ROUTE_HINT.endNode()).isEqualTo(PUBKEY_4);
    }

    @Test
    void channelId() {
        assertThat(ROUTE_HINT.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void baseFee() {
        assertThat(ROUTE_HINT.baseFee()).isEqualTo(Coins.NONE);
    }

    @Test
    void feeRate() {
        assertThat(ROUTE_HINT.feeRate()).isEqualTo(123);
    }

    @Test
    void cltvExpiryDelta() {
        assertThat(ROUTE_HINT.cltvExpiryDelta()).isEqualTo(9);
    }

}
