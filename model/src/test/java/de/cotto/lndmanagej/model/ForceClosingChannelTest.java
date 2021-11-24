package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.HTLC_OUTPOINTS;
import static de.cotto.lndmanagej.model.OpenCloseStatus.FORCE_CLOSING;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class ForceClosingChannelTest {
    @Test
    void create() {
        assertThat(new ForceClosingChannel(
                CHANNEL_ID,
                CHANNEL_POINT,
                CAPACITY,
                PUBKEY,
                PUBKEY_2,
                TRANSACTION_HASH_3,
                HTLC_OUTPOINTS,
                LOCAL)
        ).isEqualTo(FORCE_CLOSING_CHANNEL);
    }

    @Test
    void getId() {
        assertThat(FORCE_CLOSING_CHANNEL.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getRemotePubkey() {
        assertThat(FORCE_CLOSING_CHANNEL.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(FORCE_CLOSING_CHANNEL.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getChannelPoint() {
        assertThat(FORCE_CLOSING_CHANNEL.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getPubkeys() {
        assertThat(FORCE_CLOSING_CHANNEL.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void getHtlcOutpoints() {
        assertThat(FORCE_CLOSING_CHANNEL.getHtlcOutpoints()).isEqualTo(HTLC_OUTPOINTS);
    }

    @Test
    void getStatus() {
        assertThat(FORCE_CLOSING_CHANNEL.getStatus())
                .isEqualTo(new ChannelStatus(false, false, false, FORCE_CLOSING));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ForceClosingChannel.class).usingGetClass().verify();
    }
}