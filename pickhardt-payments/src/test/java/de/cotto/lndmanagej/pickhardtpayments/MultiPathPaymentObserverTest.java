package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
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

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentObserverTest {
    @InjectMocks
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    private LiquidityInformationUpdater liquidityInformationUpdater;

    @Test
    void cancels_in_flight_on_error() {
        HexString paymentHash = DECODED_PAYMENT_REQUEST.paymentHash();
        SendToRouteObserver sendToRouteObserver =
                multiPathPaymentObserver.getFor(ROUTE, paymentHash);
        sendToRouteObserver.onError(new NullPointerException());
        assertThat(multiPathPaymentObserver.getInFlight(paymentHash)).isEqualTo(Coins.NONE);
    }

    @Test
    void cancels_in_flight_using_liquidity_information_updater_on_error() {
        SendToRouteObserver sendToRouteObserver =
                multiPathPaymentObserver.getFor(ROUTE, DECODED_PAYMENT_REQUEST.paymentHash());
        sendToRouteObserver.onError(new NullPointerException());
        verify(liquidityInformationUpdater).removeInFlight(hops());
    }

    @Test
    void accepts_value() {
        SendToRouteObserver sendToRouteObserver =
                multiPathPaymentObserver.getFor(ROUTE, DECODED_PAYMENT_REQUEST.paymentHash());
        assertThatCode(
                () -> sendToRouteObserver.onValue(HexString.EMPTY, FailureCode.UNKNOWN_FAILURE)
        ).doesNotThrowAnyException();
    }

    @Test
    void inFlight_initially_zero() {
        assertThat(multiPathPaymentObserver.getInFlight(DECODED_PAYMENT_REQUEST.paymentHash()))
                .isEqualTo(Coins.NONE);
    }

    @Test
    void inFlight_initialized_with_route_amount() {
        HexString paymentHash = DECODED_PAYMENT_REQUEST.paymentHash();
        multiPathPaymentObserver.getFor(ROUTE, paymentHash);
        assertThat(multiPathPaymentObserver.getInFlight(paymentHash)).isEqualTo(ROUTE.getAmount());
    }

    @Test
    void inFlight_reset_on_success() {
        HexString paymentHash = DECODED_PAYMENT_REQUEST.paymentHash();
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, paymentHash);
        observer.onValue(new HexString("AABBCC"), FailureCode.UNKNOWN_FAILURE);
        assertThat(multiPathPaymentObserver.getInFlight(paymentHash)).isEqualTo(Coins.NONE);
    }

    @Test
    void inFlight_reset_on_failure() {
        HexString paymentHash = DECODED_PAYMENT_REQUEST.paymentHash();
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, paymentHash);
        observer.onValue(HexString.EMPTY, FailureCode.PERMANENT_CHANNEL_FAILURE);
        assertThat(multiPathPaymentObserver.getInFlight(paymentHash)).isEqualTo(Coins.NONE);
    }

    @Test
    void inFlight_updated_with_several_routes() {
        HexString paymentHash = DECODED_PAYMENT_REQUEST.paymentHash();
        multiPathPaymentObserver.getFor(ROUTE, paymentHash);
        multiPathPaymentObserver.getFor(ROUTE_2, HexString.EMPTY);
        multiPathPaymentObserver.getFor(ROUTE_3, paymentHash);
        Coins expectedAmount = ROUTE.getAmount().add(ROUTE_3.getAmount());
        assertThat(multiPathPaymentObserver.getInFlight(paymentHash)).isEqualTo(expectedAmount);
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
