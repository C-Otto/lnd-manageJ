package de.cotto.lndmanagej.grpc;

import io.grpc.stub.StreamObserver;

class NoopObserver<T> implements StreamObserver<T> {
    public NoopObserver() {
        // default constructor
    }

    @Override
    public void onNext(T value) {
        // nothing
    }

    @Override
    public void onError(Throwable throwable) {
        // nothing
    }

    @Override
    public void onCompleted() {
        // nothing
    }
}
