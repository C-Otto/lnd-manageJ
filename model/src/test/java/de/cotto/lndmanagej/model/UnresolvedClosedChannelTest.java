package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class UnresolvedClosedChannelTest {
    @Test
    void getRemotePubkey() {
        Channel channel = ChannelFixtures.create(PUBKEY_2, PUBKEY, CHANNEL_ID);
        UnresolvedClosedChannel unresolvedClosedChannel =
                new UnresolvedClosedChannel(channel, PUBKEY);
        assertThat(unresolvedClosedChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getRemotePubkey_swapped() {
        Channel channel = ChannelFixtures.create(PUBKEY_3, PUBKEY_2, CHANNEL_ID);
        UnresolvedClosedChannel unresolvedClosedChannel =
                new UnresolvedClosedChannel(channel, PUBKEY_3);
        assertThat(unresolvedClosedChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void ownPubkey_not_in_pubkey_set() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UnresolvedClosedChannel(CHANNEL_2, PUBKEY_3))
                .withMessage("Channel must have given pubkey as peer");
    }
}