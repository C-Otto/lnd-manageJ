package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelRatingFixtures.RATING;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class NodeDtoTest {

    @Test
    void alias() {
        assertThat(NODE_DTO.alias()).isEqualTo("Node");
    }

    @Test
    void pubkey() {
        assertThat(NODE_DTO.pubkey()).isEqualTo(PUBKEY.toString());
    }

    @Test
    void online() {
        assertThat(NODE_DTO.online()).isTrue();
    }

    @Test
    void rating() {
        assertThat(NODE_DTO.rating()).isEqualTo(RATING.getValue());
    }
}
