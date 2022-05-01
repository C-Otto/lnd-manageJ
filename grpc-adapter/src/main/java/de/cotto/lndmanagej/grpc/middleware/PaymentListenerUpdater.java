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
import lnrpc.Route;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PaymentListenerUpdater {
    private final List<PaymentListener> paymentListeners;

    public PaymentListenerUpdater(List<PaymentListener> paymentListeners) {
        this.paymentListeners = paymentListeners;
    }

    public void update(ByteString preimage, Route route, Failure failure) {
        List<PaymentAttemptHop> paymentAttemptHops = route.getHopsList().stream()
                .map(this::toPaymentAttemptHop)
                .toList();
        if (preimage.isEmpty()) {
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
