package de.cotto.lndmanagej.ui.dto.warning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static de.cotto.lndmanagej.ui.dto.warning.DashboardWarningsFixture.DASHBOARD_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class DashboardWarningDtoTest {

    @Test
    void alias() {
        assertThat(DASHBOARD_WARNING.alias()).isEqualTo("Node");
    }

    @Test
    void pubkey() {
        assertThat(DASHBOARD_WARNING.pubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void nodeWarnings() {
        assertThat(DASHBOARD_WARNING.nodeWarnings()).containsExactly("This is a node warning.");
    }

    @Test
    void channelWarnings() {
        assertThat(DASHBOARD_WARNING.channelWarnings()).isEqualTo(List.of(CHANNEL_WARNING_DTO));
    }

    @Test
    void getNumberOfWarnings() {
        assertThat(DASHBOARD_WARNING.numberOfWarningItems()).isEqualTo(2);
    }

    @Test
    void node_without_alias_is_shown_with_pubkey() {
        var nodeWithoutAlias = new DashboardWarningDto(
                "",
                PUBKEY,
                List.of("This is a node warning."),
                List.of(CHANNEL_WARNING_DTO)
        );
        assertThat(nodeWithoutAlias.alias()).isEqualTo(PUBKEY.toString());
    }
}
