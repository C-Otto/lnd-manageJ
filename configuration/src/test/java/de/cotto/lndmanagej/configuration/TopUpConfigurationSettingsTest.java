package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.EXPIRY;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;

class TopUpConfigurationSettingsTest {
    private static final String SECTION_NAME = "top-up";

    @Test
    void threshold() {
        assertThat(THRESHOLD.getSection()).isEqualTo(SECTION_NAME);
        assertThat(THRESHOLD.getName()).isEqualTo("threshold_sat");
    }

    @Test
    void expiry() {
        assertThat(EXPIRY.getSection()).isEqualTo(SECTION_NAME);
        assertThat(EXPIRY.getName()).isEqualTo("expiry_seconds");
    }
}
