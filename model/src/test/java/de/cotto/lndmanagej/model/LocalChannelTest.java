package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LocalChannelTest {
    @Test
    void getRemotePubkey() {
        LocalChannel localChannel = new LocalChannel(ChannelFixtures.create(PUBKEY_2, PUBKEY, CHANNEL_ID), PUBKEY);
        assertThat(localChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getRemotePubkey_swapped() {
        LocalChannel localChannel = new LocalChannel(ChannelFixtures.create(PUBKEY_3, PUBKEY_2, CHANNEL_ID), PUBKEY_3);
        assertThat(localChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void ownPubkey_not_in_pubkey_set() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LocalChannel(CHANNEL_2, PUBKEY_3))
                .withMessage("Channel must have given pubkey as peer");
    }
}