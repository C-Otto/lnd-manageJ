package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentObserverTest {
    @InjectMocks
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    private LiquidityInformationUpdater liquidityInformationUpdater;

    @Test
    void cancels_in_flight_on_error() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.forRoute(ROUTE);
        sendToRouteObserver.accept(new NullPointerException());
        verify(liquidityInformationUpdater).removeInFlight(hops());
    }

    private List<PaymentAttemptHop> hops() {
        List<Edge> edges = ROUTE.getEdges();
        List<PaymentAttemptHop> result = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            Coins forwardAmountForHop = ROUTE.getForwardAmountForHop(i);
            PaymentAttemptHop hop = new PaymentAttemptHop(
                    Optional.of(edge.channelId()),
                    forwardAmountForHop,
                    Optional.empty()
            );
            result.add(hop);
        }
        return result;
    }
}
