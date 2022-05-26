package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.EXPIRY;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.MAX_RETRIES_AFTER_FAILURE;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.SLEEP_AFTER_FAILURE_MILLISECONDS;
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

    @Test
    void sleepAfterFailureMilliseconds() {
        assertThat(SLEEP_AFTER_FAILURE_MILLISECONDS.getSection()).isEqualTo(SECTION_NAME);
        assertThat(SLEEP_AFTER_FAILURE_MILLISECONDS.getName()).isEqualTo("sleep_after_failure_milliseconds");
    }

    @Test
    void maxRetriesAfterFailure() {
        assertThat(MAX_RETRIES_AFTER_FAILURE.getSection()).isEqualTo(SECTION_NAME);
        assertThat(MAX_RETRIES_AFTER_FAILURE.getName()).isEqualTo("max_retries_after_failure");
    }
}
