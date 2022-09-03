package de.cotto.lndmanagej.configuration;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.ENABLED;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.PIECEWISE_LINEAR_APPROXIMATIONS;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.QUANTIZATION;
import static org.assertj.core.api.Assertions.assertThat;

class PickhardtPaymentsConfigurationSettingsTest {
    private static final String SECTION_NAME = "pickhardt-payments";

    @Test
    void quantization() {
        assertThat(QUANTIZATION.getSection()).isEqualTo(SECTION_NAME);
        assertThat(QUANTIZATION.getName()).isEqualTo("quantization");
    }

    @Test
    void piecewiseLinearApproximations() {
        assertThat(PIECEWISE_LINEAR_APPROXIMATIONS.getSection()).isEqualTo(SECTION_NAME);
        assertThat(PIECEWISE_LINEAR_APPROXIMATIONS.getName()).isEqualTo("piecewise_linear_approximations");
    }

    @Test
    void enabled() {
        assertThat(ENABLED.getSection()).isEqualTo(SECTION_NAME);
        assertThat(ENABLED.getName()).isEqualTo("enabled");
    }
}
