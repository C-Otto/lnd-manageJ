package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.USE_MISSION_CONTROL;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_CHANGES_THRESHOLD;
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
    private static final String LND_SECTION = "lnd";
    private static final String COMMIT_CLAIMED_STRING
            = "COMMIT:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    private static final String ANCHOR_CLAIMED_STRING
            = "ANCHOR:CLAIMED:abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";

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

    @Nested
    class LndConfigurationOptions {
        @Test
        void getLndMacaroonFile_defaults_to_empty() {
            assertThat(configurationService.getLndMacaroonFile()).isEmpty();
        }

        @Test
        void getLndMacaroonFile() {
            when(iniFileReader.getValues(LND_SECTION))
                    .thenReturn(Map.of("macaroon_file", Set.of("some string")));
            assertThat(configurationService.getLndMacaroonFile()).contains("some string");
        }

        @Test
        void getLndCertFile_defaults_to_empty() {
            assertThat(configurationService.getLndCertFile()).isEmpty();
        }

        @Test
        void getLndCertFile() {
            when(iniFileReader.getValues(LND_SECTION))
                    .thenReturn(Map.of("cert_file", Set.of("foo")));
            assertThat(configurationService.getLndCertFile()).contains("foo");
        }

        @Test
        void getLndPort_defaults_to_empty() {
            assertThat(configurationService.getLndPort()).isEmpty();
        }

        @Test
        void getLndPort_defaults_not_integer() {
            when(iniFileReader.getValues(LND_SECTION)).thenReturn(Map.of("port", Set.of("x")));
            assertThat(configurationService.getLndPort()).isEmpty();
        }

        @Test
        void getLndPort() {
            when(iniFileReader.getValues(LND_SECTION)).thenReturn(Map.of("port", Set.of("123")));
            assertThat(configurationService.getLndPort()).contains(123);
        }

        @Test
        void getHost_defaults_to_empty() {
            assertThat(configurationService.getLndHost()).isEmpty();
        }

        @Test
        @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
        void getLndHost() {
            when(iniFileReader.getValues(LND_SECTION)).thenReturn(Map.of("host", Set.of("10.0.2.2")));
            assertThat(configurationService.getLndHost()).contains("10.0.2.2");
        }
    }

    @Test
    void getIntegerValue_defaults_to_empty() {
        assertThat(configurationService.getIntegerValue(ONLINE_CHANGES_THRESHOLD)).isEmpty();
    }

    @Test
    void getIntegerValue() {
        int expectedValue = 42;
        ConfigurationSetting setting = ONLINE_CHANGES_THRESHOLD;
        String name = setting.getName();
        when(iniFileReader.getValues(WARNINGS_SECTION)).thenReturn(Map.of(name, Set.of(String.valueOf(expectedValue))));
        assertThat(configurationService.getIntegerValue(setting)).contains(expectedValue);
    }

    @Test
    void getIntegerValue_not_integer() {
        ConfigurationSetting setting = ONLINE_CHANGES_THRESHOLD;
        String name = setting.getName();
        when(iniFileReader.getValues(WARNINGS_SECTION)).thenReturn(Map.of(name, Set.of("x")));
        assertThat(configurationService.getIntegerValue(setting)).isEmpty();
    }

    @Nested
    class GetBooleanValue {
        @Test
        void yes() {
            assertBooleanValue("yes", true);
        }

        @Test
        void capital_yes() {
            assertBooleanValue("Yes", true);
        }

        @Test
        void all_caps_yes() {
            assertBooleanValue("YES", true);
        }

        @Test
        void just_true() {
            assertBooleanValue("true", true);
        }

        @Test
        void capital_true() {
            assertBooleanValue("True", true);
        }

        @Test
        void all_caps_true() {
            assertBooleanValue("TRUE", true);
        }

        @Test
        void no() {
            assertBooleanValue("no", false);
        }

        @Test
        void capital_no() {
            assertBooleanValue("No", false);
        }

        @Test
        void all_caps_no() {
            assertBooleanValue("NO", false);
        }

        @Test
        void just_false() {
            assertBooleanValue("false", false);
        }

        @Test
        void false_with_whitespace_suffix() {
            assertBooleanValue("false \t", false);
        }

        @Test
        void false_with_whitespace_prefix() {
            assertBooleanValue("   \t  false ", false);
        }

        @Test
        void capital_false() {
            assertBooleanValue("False", false);
        }

        @Test
        void all_caps_false() {
            assertBooleanValue("true", true);
        }

        @Test
        void other_string() {
            ConfigurationSetting setting = USE_MISSION_CONTROL;
            String name = setting.getName();
            when(iniFileReader.getValues(setting.getSection())).thenReturn(Map.of(name, Set.of("maybe")));
            assertThat(configurationService.getBooleanValue(setting)).isEmpty();
        }

        @Test
        void not_set() {
            assertThat(configurationService.getBooleanValue(USE_MISSION_CONTROL)).isEmpty();
        }

        private void assertBooleanValue(String stringValue, boolean expectedValue) {
            ConfigurationSetting setting = USE_MISSION_CONTROL;
            String name = setting.getName();
            when(iniFileReader.getValues(setting.getSection())).thenReturn(Map.of(name, Set.of(stringValue)));
            assertThat(configurationService.getBooleanValue(setting)).contains(expectedValue);
        }
    }
}
