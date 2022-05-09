package de.cotto.lndmanagej.grpc;

import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

class ErrorReporter<T> implements StreamObserver<T> {
    private final Consumer<Throwable> consumer;

    public ErrorReporter(Consumer<Throwable> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onNext(T value) {
        // nothing
    }

    @Override
    public void onError(Throwable throwable) {
        consumer.accept(throwable);
    }

    @Override
    public void onCompleted() {
        // nothing
    }
}
