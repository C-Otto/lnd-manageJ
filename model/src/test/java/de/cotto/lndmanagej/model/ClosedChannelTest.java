package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.CLOSED_CHANNEL_UNRESOLVED_ID;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClosedChannelTest {
    @Test
    void create_with_implicit_channel_id() {
        assertThat(ClosedChannel.create(UNRESOLVED_CLOSED_CHANNEL)).isEqualTo(CLOSED_CHANNEL);
    }

    @Test
    void create_with_unresolved_channel_id() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ClosedChannel.create(CLOSED_CHANNEL_UNRESOLVED_ID))
                .withMessage("Channel ID must be resolved");
    }

    @Test
    void create_with_explicit_channel_id() {
        assertThat(ClosedChannel.create(CLOSED_CHANNEL_UNRESOLVED_ID, CHANNEL_ID)).isEqualTo(CLOSED_CHANNEL);
    }

    @Test
    void create_with_explicit_channel_id_retains_remote_pubkey() {
        ClosedChannel closedChannel = ClosedChannel.create(CLOSED_CHANNEL_UNRESOLVED_ID, CHANNEL_ID);
        assertThat(closedChannel.getRemotePubkey()).isEqualTo(CLOSED_CHANNEL_UNRESOLVED_ID.getRemotePubkey());
    }

    @Test
    void create_with_explicit_unresolved_channel_id() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ClosedChannel.create(CLOSED_CHANNEL_UNRESOLVED_ID, ChannelId.UNRESOLVED))
                .withMessage("Channel ID must be resolved");
    }

    @Test
    void getId() {
        assertThat(CLOSED_CHANNEL.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getRemotePubkey() {
        assertThat(CLOSED_CHANNEL.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(CLOSED_CHANNEL.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getChannelPoint() {
        assertThat(CLOSED_CHANNEL.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getPubkeys() {
        assertThat(CLOSED_CHANNEL.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ClosedChannel.class).usingGetClass().verify();
    }
}