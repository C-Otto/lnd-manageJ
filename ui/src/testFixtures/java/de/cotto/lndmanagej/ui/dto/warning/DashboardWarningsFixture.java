package de.cotto.lndmanagej.ui.dto.warning;

import java.util.List;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;

public class DashboardWarningsFixture {
    public static final DashboardWarningDto DASHBOARD_WARNING = new DashboardWarningDto(
            "Node",
            PUBKEY,
            List.of("This is a node warning."),
            List.of(CHANNEL_WARNING_DTO)
    );

}
