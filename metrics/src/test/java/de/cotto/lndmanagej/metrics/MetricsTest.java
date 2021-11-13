package de.cotto.lndmanagej.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class MetricsTest {

    private final Metrics metrics = new Metrics();

    @Test
    void mark() {
        assertThatCode(() -> metrics.mark("x")).doesNotThrowAnyException();
    }

    @Test
    void shutdown() {
        assertThatCode(metrics::shutdown).doesNotThrowAnyException();
    }
}