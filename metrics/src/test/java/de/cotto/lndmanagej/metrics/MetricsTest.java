package de.cotto.lndmanagej.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MetricsTest {

    private final Metrics metrics = new Metrics(registry);

    @Test
    void mark() {
        assertThatCode(() -> metrics.mark("x")).doesNotThrowAnyException();
    }

    @Test
    void timer() {
        String name = "timer.name";
        metrics.timer(String.class, name).time(this::sleep);
        assertThat(metrics.timer(String.class, name).getCount()).isEqualTo(1);
    }

    @Test
    void shutdown() {
        assertThatCode(metrics::shutdown).doesNotThrowAnyException();
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}