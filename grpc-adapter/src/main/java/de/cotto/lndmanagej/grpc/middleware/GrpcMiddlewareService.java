package de.cotto.lndmanagej.grpc.middleware;

import de.cotto.lndmanagej.grpc.GrpcService;
import io.grpc.stub.StreamObserver;
import lnrpc.RPCMiddlewareResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class GrpcMiddlewareService implements ObserverIsDoneListener {
    private final GrpcService grpcService;
    private final Collection<RequestListener<?>> requestListeners;
    private final Collection<ResponseListener<?>> responseListeners;
    private boolean connected;

    public GrpcMiddlewareService(
            GrpcService grpcService,
            Collection<RequestListener<?>> requestListeners,
            Collection<ResponseListener<?>> responseListeners
    ) {
        this.grpcService = grpcService;
        this.requestListeners = requestListeners;
        this.responseListeners = responseListeners;
        registerMiddleware();
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void onIsDone() {
        connected = false;
        sleep();
        registerMiddleware();
    }

    private void registerMiddleware() {
        connected = true;
        RequestAndResponseStreamObserver requestAndResponseStreamObserver = new RequestAndResponseStreamObserver();
        StreamObserver<RPCMiddlewareResponse> responseObserver =
                grpcService.registerMiddleware(requestAndResponseStreamObserver);
        requestAndResponseStreamObserver.initialize(responseObserver, this);
        responseListeners.forEach(requestAndResponseStreamObserver::addResponseListener);
        requestListeners.forEach(requestAndResponseStreamObserver::addRequestListener);
    }

    private void sleep() {
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
