package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.Failure;
import lnrpc.Hop;
import org.springframework.stereotype.Component;
import routerrpc.RouterOuterClass.SendToRouteRequest;
import routerrpc.RouterOuterClass.SendToRouteResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SendToRouteListener extends RequestResponseListener<SendToRouteRequest, SendToRouteResponse> {
    private final Map<Long, SendToRouteRequest> requests = new LinkedHashMap<>();
    private final List<PaymentListener> paymentListeners;

    public SendToRouteListener(List<PaymentListener> paymentListeners) {
        super(
                SendToRouteRequest.getDescriptor().getFullName(),
                SendToRouteRequest::parseFrom,
                SendToRouteResponse.getDescriptor().getFullName(),
                SendToRouteResponse::parseFrom
        );
        this.paymentListeners = paymentListeners;
    }

    @Override
    public void acceptRequest(SendToRouteRequest request, long requestId) {
        requests.put(requestId, request);
    }

    @Override
    public void acceptResponse(SendToRouteResponse response, long requestId) {
        SendToRouteRequest request = requests.remove(requestId);
        if (request == null) {
            return;
        }
        ByteString preimage = response.getPreimage();

        List<PaymentAttemptHop> paymentAttemptHops = request.getRoute().getHopsList().stream()
                .map(this::toPaymentAttemptHop)
                .toList();
        if (preimage.isEmpty()) {
            Failure failure = response.getFailure();
            FailureCode failureCode = new FailureCode(failure.getCodeValue());
            int failureSourceIndex = failure.getFailureSourceIndex();
            paymentListeners.forEach(
                    paymentListener -> paymentListener.failure(paymentAttemptHops, failureCode, failureSourceIndex)
            );
        } else {
            paymentListeners.forEach(
                    listener -> listener.success(new HexString(preimage.toByteArray()), paymentAttemptHops)
            );
        }
    }

    private PaymentAttemptHop toPaymentAttemptHop(Hop hop) {
        Optional<Pubkey> optionalPubkey;
        if (hop.getPubKey().isBlank()) {
            optionalPubkey = Optional.empty();
        } else {
            optionalPubkey = Optional.of(Pubkey.create(hop.getPubKey()));
        }
        Optional<ChannelId> optionalChannelId;
        long chanId = hop.getChanId();
        if (chanId > 0) {
            optionalChannelId = Optional.of(ChannelId.fromShortChannelId(chanId));
        } else {
            optionalChannelId = Optional.empty();
        }
        return new PaymentAttemptHop(
                optionalChannelId,
                Coins.ofMilliSatoshis(hop.getAmtToForwardMsat()),
                optionalPubkey
        );
    }
}
