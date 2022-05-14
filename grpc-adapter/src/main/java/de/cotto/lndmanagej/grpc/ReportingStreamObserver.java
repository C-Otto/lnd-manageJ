package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import io.grpc.stub.StreamObserver;
import lnrpc.HTLCAttempt;

class ReportingStreamObserver implements StreamObserver<HTLCAttempt> {
    private final SendToRouteObserver sendToRouteObserver;

    public ReportingStreamObserver(SendToRouteObserver sendToRouteObserver) {
        this.sendToRouteObserver = sendToRouteObserver;
    }

    @Override
    public void onNext(HTLCAttempt htlcAttempt) {
        HexString preimage = new HexString(htlcAttempt.getPreimage().toByteArray());
        FailureCode failureCode = FailureCode.getFor(htlcAttempt.getFailure().getCodeValue());
        sendToRouteObserver.onValue(preimage, failureCode);
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
