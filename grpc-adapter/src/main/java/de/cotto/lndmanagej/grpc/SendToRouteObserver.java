package de.cotto.lndmanagej.grpc;

public interface SendToRouteObserver {
    void onError(Throwable throwable);

    void onValue(Object value);
}
