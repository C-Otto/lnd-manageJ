package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MultiPathPaymentObserver {
    private final LiquidityInformationUpdater liquidityInformationUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MultiPathPaymentObserver(LiquidityInformationUpdater liquidityInformationUpdater) {
        this.liquidityInformationUpdater = liquidityInformationUpdater;
    }

    public SendToRouteObserver forRoute(Route route) {
        return new SendToRouteObserverImpl(route);
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

        public SendToRouteObserverImpl(Route route) {
            this.route = route;
        }

        @Override
        public void onError(Throwable throwable) {
            logger.warn("Send to route failed for route {}: ", route, throwable);
            liquidityInformationUpdater.removeInFlight(topPaymentAttemptHops(route));
        }

        @Override
        public void onValue(Object value) {
            logger.info("Got value {}: ", value);
        }
    }
}
