package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.MetricsBuilder;
import org.springframework.stereotype.Component;
import routerrpc.RouterGrpc;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.SubscribeHtlcEventsRequest;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

@Component
public class GrpcRouterService extends GrpcBase {
    private final Meter meter;

    private final RouterGrpc.RouterBlockingStub routerStub;

    public GrpcRouterService(LndConfiguration lndConfiguration, MetricsBuilder metricsBuilder) throws IOException {
        super(lndConfiguration);
        routerStub = stubCreator.getRouterStub();
        meter = metricsBuilder.getMetric(MetricRegistry.name(getClass(), "subscribeHtlcEvents"));
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    Iterator<RouterOuterClass.HtlcEvent> getHtlcEvents() {
        meter.mark();
        return get(() -> routerStub.subscribeHtlcEvents(SubscribeHtlcEventsRequest.getDefaultInstance()))
                .orElse(Collections.emptyIterator());
    }
}
