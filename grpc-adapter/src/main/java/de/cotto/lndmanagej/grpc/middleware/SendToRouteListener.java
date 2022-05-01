package de.cotto.lndmanagej.grpc.middleware;

import org.springframework.stereotype.Component;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.SendToRouteRequest;
import routerrpc.RouterOuterClass.SendToRouteResponse;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SendToRouteListener extends RequestResponseListener<SendToRouteRequest, SendToRouteResponse> {
    private final Map<Long, SendToRouteRequest> requests = new LinkedHashMap<>();
    private final PaymentListenerUpdater paymentListenerUpdater;

    public SendToRouteListener(PaymentListenerUpdater paymentListenerUpdater) {
        super(
                SendToRouteRequest.getDescriptor().getFullName(),
                SendToRouteRequest::parseFrom,
                SendToRouteResponse.getDescriptor().getFullName(),
                SendToRouteResponse::parseFrom
        );
        this.paymentListenerUpdater = paymentListenerUpdater;
    }

    @Override
    public void acceptRequest(SendToRouteRequest request, long requestId) {
        requests.put(requestId, request);
    }

    @Override
    public void acceptResponse(SendToRouteResponse response, long requestId) {
        RouterOuterClass.SendToRouteRequest request = requests.remove(requestId);
        if (request == null) {
            return;
        }
        paymentListenerUpdater.update(response.getPreimage(), request.getRoute(), response.getFailure());
    }
}
