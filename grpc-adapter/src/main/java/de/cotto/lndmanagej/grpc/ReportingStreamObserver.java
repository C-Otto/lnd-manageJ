package de.cotto.lndmanagej.grpc;

import io.grpc.stub.StreamObserver;

class ReportingStreamObserver<T> implements StreamObserver<T> {
    private final SendToRouteObserver sendToRouteObserver;

    public ReportingStreamObserver(SendToRouteObserver sendToRouteObserver) {
        this.sendToRouteObserver = sendToRouteObserver;
    }

    @Override
    public void onNext(T value) {
        sendToRouteObserver.onValue(value);
    }

    @Override
    public void onError(Throwable throwable) {
        sendToRouteObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        // nothing
    }
}
