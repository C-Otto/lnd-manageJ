package de.cotto.lndmanagej.hardcoded;

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
class HardcodedServiceTest {
    private static final String RESOLUTIONS_SECTION = "resolutions";
    private static final String ALIASES_SECTION = "aliases";
    private static final String COMMIT_CLAIMED_STRING
            = "COMMIT:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    private static final String ANCHOR_CLAIMED_STRING
            = "ANCHOR:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";

    @InjectMocks
    private HardcodedService hardcodedService;

    @Mock
    private IniFileReader iniFileReader;

    @Test
    void getResolutions_empty() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of());
        assertThat(hardcodedService.getResolutions(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getResolutions_one_resolution_short_channel_id() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(String.valueOf(CHANNEL_ID.getShortChannelId()), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(hardcodedService.getResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getResolutions_one_resolution_compact_form() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(CHANNEL_ID.getCompactForm(), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(hardcodedService.getResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getResolutions_one_resolution_compact_form_lnd() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION))
                .thenReturn(Map.of(CHANNEL_ID.getCompactFormLnd(), Set.of(COMMIT_CLAIMED_STRING)));
        assertThat(hardcodedService.getResolutions(CHANNEL_ID)).containsExactly(COMMIT_CLAIMED);
    }

    @Test
    void getResolutions_two_resolutions() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of(
                CHANNEL_ID.getCompactFormLnd(),
                Set.of(COMMIT_CLAIMED_STRING, ANCHOR_CLAIMED_STRING)
        ));
        assertThat(hardcodedService.getResolutions(CHANNEL_ID))
                .containsExactlyInAnyOrder(COMMIT_CLAIMED, ANCHOR_CLAIMED);
    }

    @Test
    void getResolutions_bogus_string() {
        when(iniFileReader.getValues(RESOLUTIONS_SECTION)).thenReturn(Map.of(
                CHANNEL_ID.getCompactFormLnd(),
                Set.of("hello", "hello:peter", "a:b:c")
        ));
        assertThat(hardcodedService.getResolutions(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getAlias_not_known() {
        assertThat(hardcodedService.getAlias(PUBKEY)).isEmpty();
    }

    @Test
    void getAlias() {
        String expected = "hardcoded alias";
        when(iniFileReader.getValues(ALIASES_SECTION)).thenReturn(Map.of(PUBKEY.toString(), Set.of(expected)));
        assertThat(hardcodedService.getAlias(PUBKEY)).contains(expected);
    }

    @Test
    void getAlias_two_in_config() {
        String first = "a";
        String second = "b";
        when(iniFileReader.getValues(ALIASES_SECTION)).thenReturn(Map.of(PUBKEY.toString(), Set.of(first, second)));
        String actual = hardcodedService.getAlias(PUBKEY).orElseThrow();
        assertThat(Set.of(first, second).contains(actual)).isTrue();
    }
}
