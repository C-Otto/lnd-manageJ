package de.cotto.lndmanagej.ui.dto.warning;

import java.util.Set;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO_2;

public class DashboardWarningsFixture {
    public static final DashboardWarningDto DASHBOARD_WARNING = new DashboardWarningDto(
            "Node",
            PUBKEY,
            Set.of("This is a node warning."),
            Set.of(CHANNEL_WARNING_DTO)
    );

    public static final DashboardWarningDto DASHBOARD_WARNING_2 = new DashboardWarningDto(
            "Node 2",
            PUBKEY_2,
            Set.of("This is another node warning."),
            Set.of(CHANNEL_WARNING_DTO, CHANNEL_WARNING_DTO_2)
    );

    public static final DashboardWarningDto DASHBOARD_WARNING_3 = new DashboardWarningDto(
            "Node 3",
            PUBKEY_3,
            Set.of("This is only a node warning."),
            Set.of()
    );

}
