package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;

class NoopSendToRouteObserver implements SendToRouteObserver {
    public NoopSendToRouteObserver() {
        // default constructor
    }

    @Override
    public void onError(Throwable throwable) {
        // nothing
    }

    @Override
    public void onValue(HexString preimage, FailureCode failureCode) {
        // nothing
    }
}
