package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_BREACH;
import static de.cotto.lndmanagej.ui.dto.CloseType.BREACH_FORCE_CLOSE;
import static de.cotto.lndmanagej.ui.dto.CloseType.COOP_CLOSE;
import static de.cotto.lndmanagej.ui.dto.CloseType.FORCE_CLOSE;
import static de.cotto.lndmanagej.ui.dto.CloseType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

class CloseTypeTest {

    @Test
    void enumsExist() {
        assertThat(CloseType.values()).containsExactlyInAnyOrder(COOP_CLOSE, FORCE_CLOSE, BREACH_FORCE_CLOSE, UNKNOWN);
    }

    @Test
    void getType_coop() {
        assertThat(CloseType.getType(CLOSED_CHANNEL)).isEqualTo(COOP_CLOSE);
    }

    @Test
    void getType_force_closed() {
        assertThat(CloseType.getType(FORCE_CLOSED_CHANNEL)).isEqualTo(FORCE_CLOSE);
    }

    @Test
    void getType_breach_force_closed() {
        assertThat(CloseType.getType(FORCE_CLOSED_CHANNEL_BREACH)).isEqualTo(BREACH_FORCE_CLOSE);
    }

    @Test
    void getType_unknown() {
        assertThat(CloseType.getType(null)).isEqualTo(UNKNOWN);
    }

    @Test
    void testToString() {
        assertThat(COOP_CLOSE.toString()).isEqualTo("coop");
    }

}