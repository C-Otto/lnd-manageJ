package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.DAYS_FOR_ANALYSIS;
import static de.cotto.lndmanagej.configuration.RatingConfigurationSettings.MIN_AGE_DAYS_FOR_ANALYSIS;
import static org.assertj.core.api.Assertions.assertThat;

class RatingConfigurationSettingsTest {
    private static final String SECTION_NAME = "ratings";

    @Test
    void minAgeDaysForAnalysis() {
        assertThat(MIN_AGE_DAYS_FOR_ANALYSIS.getSection()).isEqualTo(SECTION_NAME);
        assertThat(MIN_AGE_DAYS_FOR_ANALYSIS.getName()).isEqualTo("minimum_age_in_days");
    }

    @Test
    void daysForAnalysis() {
        assertThat(DAYS_FOR_ANALYSIS.getSection()).isEqualTo(SECTION_NAME);
        assertThat(DAYS_FOR_ANALYSIS.getName()).isEqualTo("days_for_analysis");
    }

}