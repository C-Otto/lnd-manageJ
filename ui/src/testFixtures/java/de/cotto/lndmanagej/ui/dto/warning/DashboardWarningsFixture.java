package de.cotto.lndmanagej.ui.dto.warning;

import java.util.List;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO_2;

public class DashboardWarningsFixture {
    public static final DashboardWarningDto DASHBOARD_WARNING = new DashboardWarningDto(
            "Node",
            PUBKEY,
            List.of("This is a node warning."),
            List.of(CHANNEL_WARNING_DTO)
    );

    public static final DashboardWarningDto DASHBOARD_WARNING_2 = new DashboardWarningDto(
            "Node 2",
            PUBKEY_2,
            List.of("This is another node warning."),
            List.of(CHANNEL_WARNING_DTO, CHANNEL_WARNING_DTO_2)
    );

    public static final DashboardWarningDto DASHBOARD_WARNING_3 = new DashboardWarningDto(
            "Node 3",
            PUBKEY_3,
            List.of("This is only a node warning."),
            List.of()
    );

}
