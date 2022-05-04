package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import io.grpc.stub.StreamObserver;
import lnrpc.HTLCAttempt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import routerrpc.RouterGrpc;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.QueryMissionControlRequest;
import routerrpc.RouterOuterClass.QueryMissionControlResponse;
import routerrpc.RouterOuterClass.SubscribeHtlcEventsRequest;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

@Component
public class GrpcRouterService extends GrpcBase {
    private final RouterGrpc.RouterBlockingStub routerStub;
    private final RouterGrpc.RouterStub nonBlockingRouterStub;

    public GrpcRouterService(
            ConfigurationService configurationService,
            @Value("${user.home}") String homeDirectory
    ) throws IOException {
        super(configurationService, homeDirectory);
        routerStub = stubCreator.getRouterStub();
        nonBlockingRouterStub = stubCreator.getNonBlockingRouterStub();
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

    @Timed
    public Optional<QueryMissionControlResponse> queryMissionControl() {
        QueryMissionControlRequest request = QueryMissionControlRequest.getDefaultInstance();
        return get(() -> routerStub.queryMissionControl(request));
    }

    @Timed
    public void sendToRoute(RouterOuterClass.SendToRouteRequest request, StreamObserver<HTLCAttempt> streamObserver) {
        nonBlockingRouterStub.sendToRouteV2(request, streamObserver);
    }
}
