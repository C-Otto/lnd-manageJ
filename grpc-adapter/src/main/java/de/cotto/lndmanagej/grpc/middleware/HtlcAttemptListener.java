package de.cotto.lndmanagej.grpc.middleware;

import lnrpc.HTLCAttempt;
import org.springframework.stereotype.Component;

@Component
public class HtlcAttemptListener extends AbstractResponseListener<HTLCAttempt> {

    private final PaymentListenerUpdater paymentListenerUpdater;

    public HtlcAttemptListener(PaymentListenerUpdater paymentListenerUpdater) {
        super(HTLCAttempt.getDescriptor().getFullName(), HTLCAttempt::parseFrom);
        this.paymentListenerUpdater = paymentListenerUpdater;
    }

    @Override
    public void acceptResponse(HTLCAttempt response, long requestId) {
        paymentListenerUpdater.update(response.getPreimage(), response.getRoute(), response.getFailure());
    }

}
