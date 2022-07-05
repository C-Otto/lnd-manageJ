package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_BALANCE_FLUCTUATION_WARNING;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_NUM_UPDATES_WARNING;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningServiceImplTest {

    public static final List<String> THREE_NODE_WARNINGS = List.of("No flow in the past 16 days",
            "Node changed between online and offline 123 times in the past 7 days",
            "Node has been online 51% in the past 14 days"
    );

    @InjectMocks
    private WarningServiceImpl warningService;

    @Mock
    private NodeService nodeService;

    @Mock
    private NodeWarningsService nodeWarningsService;

    @Mock
    private ChannelWarningsService channelWarningsService;

    @Test
    void getWarnings_noWarnings_noWarnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of());

        assertThat(warningService.getWarnings()).isEmpty();
    }

    @Test
    void getWarnings_onlyNodeWarnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS));
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of());

        assertThat(warningService.getWarnings()).containsExactly(new DashboardWarningDto(
                "Node",
                PUBKEY,
                THREE_NODE_WARNINGS,
                List.of()
        ));
    }

    @Test
    void getWarnings_onlyChannelWarnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of(
                LOCAL_OPEN_CHANNEL, new ChannelWarnings(CHANNEL_NUM_UPDATES_WARNING),
                LOCAL_OPEN_CHANNEL_2, new ChannelWarnings(CHANNEL_BALANCE_FLUCTUATION_WARNING))
        );
        String alias = "Another Node";
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(alias);

        assertThat(warningService.getWarnings()).containsExactly(new DashboardWarningDto(
                alias,
                PUBKEY_2,
                List.of(),
                List.of(CHANNEL_WARNING_DTO, CHANNEL_WARNING_DTO_2)
        ));
    }

    @Test
    void getWarnings_nodeAndChannelWarnings_combined() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS, NODE_2, NODE_WARNINGS));
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of(LOCAL_OPEN_CHANNEL, CHANNEL_WARNINGS));

        DashboardWarningDto nodeWarning = new DashboardWarningDto(
                "Node",
                PUBKEY,
                THREE_NODE_WARNINGS,
                List.of()
        );
        DashboardWarningDto anotherNodeWarning = new DashboardWarningDto(
                "Another Node",
                PUBKEY_2,
                THREE_NODE_WARNINGS,
                List.of(CHANNEL_WARNING_DTO)
        );
        assertThat(warningService.getWarnings()).containsExactly(nodeWarning, anotherNodeWarning);
    }
}