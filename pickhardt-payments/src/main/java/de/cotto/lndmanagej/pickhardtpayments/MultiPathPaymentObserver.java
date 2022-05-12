package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiPathPaymentObserver {
    private final LiquidityInformationUpdater liquidityInformationUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<HexString, Coins> inFlight = new ConcurrentHashMap<>();

    public MultiPathPaymentObserver(LiquidityInformationUpdater liquidityInformationUpdater) {
        this.liquidityInformationUpdater = liquidityInformationUpdater;
    }

    public SendToRouteObserver getFor(Route route, HexString paymentHash) {
        return new SendToRouteObserverImpl(route, paymentHash);
    }

    public Coins getInFlight(HexString paymentHash) {
        return inFlight.getOrDefault(paymentHash, Coins.NONE);
    }

    private void addInFlight(HexString paymentHash, Coins amount) {
        inFlight.compute(paymentHash, (key, value) -> value == null ? amount : amount.add(value));
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
            logger.warn("Send to route failed for route {}: ", route, throwable);
            liquidityInformationUpdater.removeInFlight(topPaymentAttemptHops(route));
            addInFlight(paymentHash, route.getAmount().negate());
        }

        @Override
        public void onValue(HexString preimage) {
            addInFlight(paymentHash, route.getAmount().negate());
        }
    }
}
