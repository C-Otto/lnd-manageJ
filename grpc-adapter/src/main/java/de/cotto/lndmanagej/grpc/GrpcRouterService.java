package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;
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
    private final RouterGrpc.RouterBlockingStub routerStub;

    public GrpcRouterService(LndConfiguration lndConfiguration, Metrics metrics) throws IOException {
        super(lndConfiguration, metrics);
        routerStub = stubCreator.getRouterStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    Iterator<RouterOuterClass.HtlcEvent> getHtlcEvents() {
        return get("subscribeHtlcEvents",
                () -> routerStub.subscribeHtlcEvents(SubscribeHtlcEventsRequest.getDefaultInstance())
        ).orElse(Collections.emptyIterator());
    }
}
