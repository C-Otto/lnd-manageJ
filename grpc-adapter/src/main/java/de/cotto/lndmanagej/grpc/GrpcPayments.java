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

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return getPaymentOptionals(offset, false)
                .map(payments -> payments.flatMap(Optional::stream).toList());
    }

    public Optional<List<Optional<Payment>>> getAllPaymentsAfter(long offset) {
        return getPaymentOptionals(offset, true)
                .map(Stream::toList);
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

    private Optional<Stream<Optional<Payment>>> getPaymentOptionals(long offset, boolean includeIncomplete) {
        ListPaymentsResponse list = grpcService.getPayments(offset, LIMIT, includeIncomplete).orElse(null);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.of(list.getPaymentsList().stream().map(this::toPayment));
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

    private Optional<Payment> toPayment(lnrpc.Payment lndPayment) {
        if (lndPayment.getStatus() != SUCCEEDED) {
            return Optional.empty();
        }
        Instant timestamp = Instant.ofEpochMilli(lndPayment.getCreationTimeNs() / 1_000);
        String paymentHash = lndPayment.getPaymentHash();
        return Optional.of(new Payment(
                lndPayment.getPaymentIndex(),
                paymentHash,
                LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC),
                Coins.ofMilliSatoshis(lndPayment.getValueMsat()),
                Coins.ofMilliSatoshis(lndPayment.getFeeMsat()),
                lndPayment.getHtlcsList().stream()
                        .filter(htlcAttempt -> htlcAttempt.getStatus() == HTLCAttempt.HTLCStatus.SUCCEEDED)
                        .map(htlcAttempt -> toPaymentRoute(htlcAttempt, paymentHash)).toList()
        ));
    }

    private PaymentRoute toPaymentRoute(HTLCAttempt htlcAttempt, String paymentHash) {
        List<Hop> hopsList = htlcAttempt.getRoute().getHopsList();
        Optional<PaymentHop> first = toHop(hopsList.get(0), paymentHash, true);
        Optional<PaymentHop> last = toHop(hopsList.get(hopsList.size() - 1), paymentHash, false);
        return new PaymentRoute(first, last);
    }

    private Optional<PaymentHop> toHop(Hop hop, String paymentHash, boolean first) {
        if (hop.getChanId() == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(new PaymentHop(
                    ChannelId.fromShortChannelId(new BigInteger(Long.toUnsignedString(hop.getChanId()))),
                    Coins.ofMilliSatoshis(hop.getAmtToForwardMsat()),
                    first
            ));
        } catch (IllegalArgumentException exception) {
            logger.error("Unable to parse hop {} in payment with payment hash {}", hop, paymentHash);
            throw exception;
        }
    }
}
