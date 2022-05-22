package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentInformation;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class MultiPathPaymentObserver {
    private final LiquidityInformationUpdater liquidityInformationUpdater;
    private final Map<HexString, PaymentInformation> map = new ConcurrentHashMap<>();
    private final Map<HexString, CountDownLatch> latches = new ConcurrentHashMap<>();

    public MultiPathPaymentObserver(LiquidityInformationUpdater liquidityInformationUpdater) {
        this.liquidityInformationUpdater = liquidityInformationUpdater;
    }

    public SendToRouteObserver getFor(Route route, HexString paymentHash) {
        return new SendToRouteObserverImpl(route, paymentHash);
    }

    public Coins getInFlight(HexString paymentHash) {
        return get(paymentHash).inFlight();
    }

    public boolean isSettled(HexString paymentHash) {
        return get(paymentHash).settled();
    }

    public Optional<FailureCode> getFailureCode(HexString paymentHash) {
        return get(paymentHash).failureCode();
    }

    private void addInFlight(HexString paymentHash, Coins amount) {
        update(paymentHash, value -> value.withAdditionalInFlight(amount));
        latches.compute(paymentHash, (key, value) -> {
            if (value == null) {
                return null;
            }
            value.countDown();
            return null;
        });
    }

    private PaymentInformation get(HexString paymentHash) {
        return map.getOrDefault(paymentHash, PaymentInformation.DEFAULT);
    }

    private void update(HexString paymentHash, Function<PaymentInformation, PaymentInformation> updater) {
        map.compute(paymentHash, (key, value) -> {
            PaymentInformation newValue = updater.apply(value == null ? PaymentInformation.DEFAULT : value);
            if (PaymentInformation.DEFAULT.equals(newValue)) {
                return null;
            }
            return newValue;
        });
    }

    private List<PaymentAttemptHop> topPaymentAttemptHops(Route route) {
        List<Edge> edges = route.getEdges();
        List<PaymentAttemptHop> result = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            Coins forwardAmountForHop = route.getForwardAmountForHop(i);
            PaymentAttemptHop hop = new PaymentAttemptHop(
                    Optional.of(edge.channelId()),
                    forwardAmountForHop,
                    Optional.empty()
            );
            result.add(hop);
        }
        return result;
    }

    public void waitForInFlightChange(Duration timeout, HexString paymentHash, Coins referenceInFlight) {
        CountDownLatch latch = getLatch(paymentHash);
        while (getInFlight(paymentHash).equals(referenceInFlight)) {
            try {
                boolean changedWithinTimeout = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (!changedWithinTimeout) {
                    return;
                }
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }

    private CountDownLatch getLatch(HexString paymentHash) {
        return latches.compute(paymentHash, (key, value) -> value == null ? new CountDownLatch(1) : value);
    }

    private class SendToRouteObserverImpl implements SendToRouteObserver {
        private final Route route;
        private final HexString paymentHash;

        public SendToRouteObserverImpl(Route route, HexString paymentHash) {
            this.route = route;
            this.paymentHash = paymentHash;
            addInFlight(paymentHash, route.getAmount());
        }

        @Override
        public void onError(Throwable throwable) {
            liquidityInformationUpdater.removeInFlight(topPaymentAttemptHops(route));
            addInFlight(paymentHash, route.getAmount().negate());
        }

        @Override
        public void onValue(HexString preimage, FailureCode failureCode) {
            if (preimage.equals(HexString.EMPTY)) {
                if (failureCode.isErrorFromFinalNode()) {
                    update(paymentHash, paymentInformation -> paymentInformation.withFailureCode(failureCode));
                }
            } else {
                update(paymentHash, PaymentInformation::withIsSettled);
            }
            addInFlight(paymentHash, route.getAmount().negate());
        }
    }

}
