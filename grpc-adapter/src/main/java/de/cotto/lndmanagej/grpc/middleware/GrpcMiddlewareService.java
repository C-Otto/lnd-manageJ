package de.cotto.lndmanagej.grpc.middleware;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings;
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
    private final ConfigurationService configurationService;
    private boolean connected;

    public GrpcMiddlewareService(
            GrpcService grpcService,
            Collection<RequestListener<?>> requestListeners,
            Collection<ResponseListener<?>> responseListeners,
            ConfigurationService configurationService
    ) {
        this.grpcService = grpcService;
        this.requestListeners = requestListeners;
        this.responseListeners = responseListeners;
        this.configurationService = configurationService;
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
        if (isDisabledInConfiguration()) {
            return;
        }
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
        } catch (InterruptedException ignored) {
            // ignore
        }
    }

    @SuppressWarnings("PMD.UnnecessaryLocalBeforeReturn")
    private boolean isDisabledInConfiguration() {
        boolean isEnabled = configurationService.getBooleanValue(PickhardtPaymentsConfigurationSettings.ENABLED)
                .orElse(false);
        return !isEnabled;
    }
}
