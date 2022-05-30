package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RouteHint;
import lnrpc.HTLCAttempt;
import lnrpc.Hop;
import lnrpc.ListPaymentsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static lnrpc.Payment.PaymentStatus.SUCCEEDED;

@Component
public class GrpcPayments {
    private static final int LIMIT = 1_000;

    private final GrpcService grpcService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public Optional<DecodedPaymentRequest> decodePaymentRequest(String paymentRequest) {
        return grpcService.decodePaymentRequest(paymentRequest)
                .map(payReq -> {
                    Instant creationTimestamp = Instant.ofEpochSecond(payReq.getTimestamp());
                    Instant expiryTimestamp = creationTimestamp.plusSeconds(payReq.getExpiry());
                    Pubkey destination = Pubkey.create(payReq.getDestination());
                    return new DecodedPaymentRequest(
                            paymentRequest,
                            (int) payReq.getCltvExpiry(),
                            payReq.getDescription(),
                            destination,
                            Coins.ofMilliSatoshis(payReq.getNumMsat()),
                            new HexString(payReq.getPaymentHash()),
                            new HexString(payReq.getPaymentAddr().toByteArray()),
                            creationTimestamp,
                            expiryTimestamp,
                            getRouteHints(destination, payReq.getRouteHintsList())
                    );
                });
    }

    private Set<RouteHint> getRouteHints(Pubkey destination, List<lnrpc.RouteHint> routeHintsList) {
        return routeHintsList.stream()
                .filter(routeHint -> routeHint.getHopHintsCount() == 1)
                .map(routeHint -> routeHint.getHopHints(0))
                .map(hopHint -> new RouteHint(
                                Pubkey.create(hopHint.getNodeId()),
                                destination,
                                ChannelId.fromShortChannelId(hopHint.getChanId()),
                                Coins.ofMilliSatoshis(hopHint.getFeeBaseMsat()),
                                hopHint.getFeeProportionalMillionths(),
                                hopHint.getCltvExpiryDelta()
                        )
                ).collect(Collectors.toSet());
    }

    private Payment toPayment(lnrpc.Payment lndPayment) {
        if (lndPayment.getStatus() != SUCCEEDED) {
            throw new IllegalStateException("");
        }
        Instant timestamp = Instant.ofEpochMilli(lndPayment.getCreationTimeNs() / 1_000);
        String paymentHash = lndPayment.getPaymentHash();
        return new Payment(
                lndPayment.getPaymentIndex(),
                paymentHash,
                LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC),
                Coins.ofMilliSatoshis(lndPayment.getValueMsat()),
                Coins.ofMilliSatoshis(lndPayment.getFeeMsat()),
                lndPayment.getHtlcsList().stream()
                        .filter(htlcAttempt -> htlcAttempt.getStatus() == HTLCAttempt.HTLCStatus.SUCCEEDED)
                        .map(htlcAttempt -> toPaymentRoute(htlcAttempt, paymentHash)).toList()
        );
    }

    private PaymentRoute toPaymentRoute(HTLCAttempt htlcAttempt, String paymentHash) {
        List<PaymentHop> hops = htlcAttempt.getRoute().getHopsList().stream()
                .filter(hop -> hop.getChanId() != 0)
                .map(hop -> toHop(hop, paymentHash))
                .toList();
        return new PaymentRoute(hops);
    }

    private PaymentHop toHop(Hop hop, String paymentHash) {
        try {
            return new PaymentHop(
                    ChannelId.fromShortChannelId(hop.getChanId()),
                    Coins.ofMilliSatoshis(hop.getAmtToForwardMsat())
            );
        } catch (IllegalArgumentException exception) {
            logger.error("Unable to parse hop {} in payment with payment hash {}", hop, paymentHash);
            throw exception;
        }
    }
}
