package de.cotto.lndmanagej.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class Metrics {
    private final MetricRegistry registry = new MetricRegistry();
    private final Slf4jReporter reporter;
    private final Map<String, Meter> meters = new LinkedHashMap<>();

    public Metrics() {
        reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(LoggerFactory.getLogger(getClass()))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void shutdown() {
        reporter.close();
    }

    public void mark(String name) {
        meters.computeIfAbsent(name, registry::meter).mark();
    }
}
