package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import lnrpc.HTLCAttempt;
import lnrpc.Hop;
import lnrpc.ListPaymentsResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static lnrpc.Payment.PaymentStatus.SUCCEEDED;

@Component
public class GrpcPayments {
    private static final int LIMIT = 1_000;

    private final GrpcService grpcService;

    public GrpcPayments(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public int getLimit() {
        return LIMIT;
    }

    public Optional<List<Payment>> getPaymentsAfter(long offset) {
        ListPaymentsResponse list = grpcService.getPayments(offset, LIMIT).orElse(null);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.getPaymentsList().stream().map(this::toPayment).toList());
    }

    private Payment toPayment(lnrpc.Payment lndPayment) {
        if (lndPayment.getStatus() != SUCCEEDED) {
            throw new IllegalStateException("");
        }
        Instant timestamp = Instant.ofEpochMilli(lndPayment.getCreationTimeNs() / 1_000);
        return new Payment(
                lndPayment.getPaymentIndex(),
                lndPayment.getPaymentHash(),
                LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC),
                Coins.ofMilliSatoshis(lndPayment.getValueMsat()),
                Coins.ofMilliSatoshis(lndPayment.getFeeMsat()),
                lndPayment.getHtlcsList().stream().map(this::toPaymentRoute).toList()
        );
    }

    private PaymentRoute toPaymentRoute(HTLCAttempt htlcAttempt) {
        List<PaymentHop> hops = htlcAttempt.getRoute().getHopsList().stream()
                .filter(hop -> hop.getChanId() != 0)
                .map(this::toHop)
                .toList();
        return new PaymentRoute(hops);
    }

    private PaymentHop toHop(Hop hop) {
        return new PaymentHop(
                ChannelId.fromShortChannelId(hop.getChanId()),
                Coins.ofMilliSatoshis(hop.getAmtToForwardMsat())
        );
    }
}
