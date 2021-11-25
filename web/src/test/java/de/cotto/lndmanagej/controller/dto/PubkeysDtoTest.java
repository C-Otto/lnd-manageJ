package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static org.assertj.core.api.Assertions.assertThat;

class PubkeysDtoTest {
    @Test
    void uses_toString_in_order() {
        List<Pubkey> expectedPubkeys =
                List.of(LOCAL_OPEN_CHANNEL.getRemotePubkey(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey());
        assertThat(new PubkeysDto(expectedPubkeys).pubkeys()).containsExactly(
                LOCAL_OPEN_CHANNEL.getRemotePubkey().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey().toString()
        );
    }
}