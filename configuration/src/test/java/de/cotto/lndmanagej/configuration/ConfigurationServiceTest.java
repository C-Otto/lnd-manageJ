package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.ResolutionFixtures.ANCHOR_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.COMMIT_CLAIMED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {
    private static final String RESOLUTIONS_SECTION = "resolutions";
    private static final String ALIASES_SECTION = "aliases";
    private static final String WARNINGS_SECTION = "warnings";
    private static final String COMMIT_CLAIMED_STRING
            = "COMMIT:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    private static final String ANCHOR_CLAIMED_STRING
            = "ANCHOR:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    private static final String CHANNEL_FLUCTUATION_LOWER_THRESHOLD = "channel_fluctuation_lower_threshold";
    private static final String CHANNEL_FLUCTUATION_UPPER_THRESHOLD = "channel_fluctuation_upper_threshold";
    private static final String MAX_NUM_UPDATES = "max_num_updates";
    private static final String NODE_FLOW_MINIMUM_DAYS_FOR_WARNING = "node_flow_minimum_days_for_warning";
    private static final String NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER = "node_flow_maximum_days_to_consider";

    @InjectMocks
    private ConfigurationService configurationService;

    @Mock
    private IniFileReader iniFileReader;

    @Test
    void getHardcodedResolutions_empty() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of());
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getHardcodedResolutions_one_resolution_short_channel_id() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(String.valueOf(CHANNEL_ID.getShortChannelId()), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getHardcodedResolutions_one_resolution_compact_form() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(CHANNEL_ID.getCompactForm(), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getHardcodedResolutions_one_resolution_compact_form_lnd() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(CHANNEL_ID.getCompactFormLnd(), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getHardcodedResolutions_two_resolutions() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of(
                CHANNEL_ID.getCompactFormLnd(),
                Set.of(COMMIT_CLAIMED_STRING, ANCHOR_CLAIMED_STRING)
        ));
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID))
                .containsExactlyInAnyOrder(COMMIT_CLAIMED, ANCHOR_CLAIMED);
    }

    @Test
    void getHardcodedResolutions_bogus_string() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of(
                CHANNEL_ID.getCompactFormLnd(),
                Set.of("hello", "hello:peter", "a:b:c")
        ));
        assertThat(configurationService.getHardcodedResolutions(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getHardcodedAlias_not_known() {
        assertThat(configurationService.getHardcodedAlias(PUBKEY)).isEmpty();
    }

    @Test
    void getHardcodedAlias() {
        String expected = "configured alias";
        when(iniFileReader.getValues(ALIASES_SECTION)).thenReturn(Map.of(PUBKEY.toString(), Set.of(expected)));
        assertThat(configurationService.getHardcodedAlias(PUBKEY)).contains(expected);
    }

    @Test
    void getHardcodedAlias_two_in_config() {
        String first = "a";
        String second = "b";
        when(iniFileReader.getValues(ALIASES_SECTION)).thenReturn(Map.of(PUBKEY.toString(), Set.of(first, second)));
        String actual = configurationService.getHardcodedAlias(PUBKEY).orElseThrow();
        assertThat(Set.of(first, second).contains(actual)).isTrue();
    }

    @Test
    void getChannelBalanceFluctuationWarningLowerThreshold_defaults_to_empty() {
        assertThat(configurationService.getChannelFluctuationWarningLowerThreshold()).isEmpty();
    }

    @Test
    void getChannelBalanceFluctuationWarningLowerThreshold() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(CHANNEL_FLUCTUATION_LOWER_THRESHOLD, Set.of("1")));
        assertThat(configurationService.getChannelFluctuationWarningLowerThreshold()).contains(1);
    }

    @Test
    void getChannelBalanceFluctuationWarningLowerThreshold_not_integer() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(CHANNEL_FLUCTUATION_LOWER_THRESHOLD, Set.of("x")));
        assertThat(configurationService.getChannelFluctuationWarningLowerThreshold()).isEmpty();
    }

    @Test
    void getChannelBalanceFluctuationWarningUpperThreshold_defaults_to_empty() {
        assertThat(configurationService.getChannelFluctuationWarningUpperThreshold()).isEmpty();
    }

    @Test
    void getChannelBalanceFluctuationWarningUpperThreshold() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(CHANNEL_FLUCTUATION_UPPER_THRESHOLD, Set.of("99")));
        assertThat(configurationService.getChannelFluctuationWarningUpperThreshold()).contains(99);
    }

    @Test
    void getChannelBalanceFluctuationWarningUpperThreshold_not_integer() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(CHANNEL_FLUCTUATION_UPPER_THRESHOLD, Set.of("x")));
        assertThat(configurationService.getChannelFluctuationWarningUpperThreshold()).isEmpty();
    }

    @Test
    void getMaxNumUpdates_defaults_to_empty() {
        assertThat(configurationService.getMaxNumUpdates()).isEmpty();
    }

    @Test
    void getMaxNumUpdates() {
        when(iniFileReader.getValues(WARNINGS_SECTION)).thenReturn(Map.of(MAX_NUM_UPDATES, Set.of("99")));
        assertThat(configurationService.getMaxNumUpdates()).contains(99);
    }

    @Test
    void getMaxNumUpdates_not_integer() {
        when(iniFileReader.getValues(WARNINGS_SECTION)).thenReturn(Map.of(MAX_NUM_UPDATES, Set.of("x")));
        assertThat(configurationService.getMaxNumUpdates()).isEmpty();
    }

    @Test
    void getNodeFlowWarningMinimumDaysForWarning_defaults_to_empty() {
        assertThat(configurationService.getNodeFlowWarningMinimumDaysForWarning()).isEmpty();
    }

    @Test
    void getNodeFlowWarningMinimumDaysForWarning() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING, Set.of("99")));
        assertThat(configurationService.getNodeFlowWarningMinimumDaysForWarning()).contains(99);
    }

    @Test
    void getNodeFlowWarningMinimumDaysForWarning_not_integer() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING, Set.of("x")));
        assertThat(configurationService.getNodeFlowWarningMinimumDaysForWarning()).isEmpty();
    }

    @Test
    void getNodeFlowWarningMaximumDaysToConsider_defaults_to_empty() {
        assertThat(configurationService.getNodeFlowWarningMaximumDaysToConsider()).isEmpty();
    }

    @Test
    void getNodeFlowWarningMaximumDaysToConsider() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER, Set.of("99")));
        assertThat(configurationService.getNodeFlowWarningMaximumDaysToConsider()).contains(99);
    }

    @Test
    void getNodeFlowWarningMaximumDaysToConsider_not_integer() {
        when(iniFileReader.getValues(WARNINGS_SECTION))
                .thenReturn(Map.of(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER, Set.of("x")));
        assertThat(configurationService.getNodeFlowWarningMaximumDaysToConsider()).isEmpty();
    }
}
