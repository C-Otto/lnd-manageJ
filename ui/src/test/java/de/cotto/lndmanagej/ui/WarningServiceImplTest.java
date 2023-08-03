package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningServiceImplTest {

    public static final List<String> THREE_NODE_WARNINGS = List.of("No flow in the past 16 days",
            "Node changed between online and offline 123 times in the past 7 days",
            "Node has been online 51% in the past 14 days"
    );
    private static final String ALIAS_FOR_NODE = "Node";
    private static final String ALIAS_FOR_PUBKEY_2 = "Another Node";

    @InjectMocks
    private WarningServiceImpl warningService;

    @Mock
    private NodeService nodeService;

    @Mock
    private NodeWarningsService nodeWarningsService;

    @Mock
    private ChannelWarningsService channelWarningsService;

    @BeforeEach
    void setUp() {
        lenient().when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_FOR_PUBKEY_2);
    }

    @Test
    void getWarnings_empty() {
        assertThat(warningService.getWarnings()).isEmpty();
    }

    @Test
    void getWarnings_only_node_warnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS));

        assertThat(warningService.getWarnings()).containsExactly(new DashboardWarningDto(
                ALIAS_FOR_NODE,
                PUBKEY,
                THREE_NODE_WARNINGS,
                List.of()
        ));
    }

    @Test
    void getWarnings_only_channel_warnings() {
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of(
                LOCAL_OPEN_CHANNEL, new ChannelWarnings(CHANNEL_NUM_UPDATES_WARNING),
                LOCAL_OPEN_CHANNEL_2, new ChannelWarnings(CHANNEL_BALANCE_FLUCTUATION_WARNING))
        );

        assertThat(warningService.getWarnings()).containsExactly(new DashboardWarningDto(
                ALIAS_FOR_PUBKEY_2,
                PUBKEY_2,
                List.of(),
                List.of(CHANNEL_WARNING_DTO, CHANNEL_WARNING_DTO_2)
        ));
    }

    @Test
    void getWarnings_node_and_channel_warnings_combined() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS, NODE_2, NODE_WARNINGS));
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of(LOCAL_OPEN_CHANNEL, CHANNEL_WARNINGS));

        DashboardWarningDto nodeWarning = new DashboardWarningDto(
                ALIAS_FOR_NODE,
                PUBKEY,
                THREE_NODE_WARNINGS,
                List.of()
        );
        DashboardWarningDto anotherNodeWarning = new DashboardWarningDto(
                ALIAS_FOR_PUBKEY_2,
                PUBKEY_2,
                THREE_NODE_WARNINGS,
                List.of(CHANNEL_WARNING_DTO)
        );
        assertThat(warningService.getWarnings()).containsExactly(nodeWarning, anotherNodeWarning);
    }
}
