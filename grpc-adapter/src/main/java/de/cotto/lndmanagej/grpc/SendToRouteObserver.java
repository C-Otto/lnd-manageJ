package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.HexString;

public interface SendToRouteObserver {
    void onError(Throwable throwable);

    void onValue(HexString preimage);
}
