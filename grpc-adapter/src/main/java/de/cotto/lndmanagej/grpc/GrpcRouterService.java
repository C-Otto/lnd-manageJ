package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.LndConfiguration;
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

    public GrpcRouterService(LndConfiguration lndConfiguration) throws IOException {
        super(lndConfiguration);
        routerStub = stubCreator.getRouterStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    @Timed
    public Iterator<RouterOuterClass.HtlcEvent> getHtlcEvents() {
        return get(() -> routerStub.subscribeHtlcEvents(SubscribeHtlcEventsRequest.getDefaultInstance()))
                .orElse(Collections.emptyIterator());
    }
}
