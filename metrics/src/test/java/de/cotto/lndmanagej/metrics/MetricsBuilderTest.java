package de.cotto.lndmanagej.metrics;

import com.codahale.metrics.Meter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MetricsBuilderTest {

    private final MetricsBuilder metricsBuilder = new MetricsBuilder();

    @Test
    void getMetric() {
        Meter meter = metricsBuilder.getMetric("x");
        meter.mark();
        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    void shutdown() {
        assertThatCode(metricsBuilder::shutdown).doesNotThrowAnyException();
    }
}