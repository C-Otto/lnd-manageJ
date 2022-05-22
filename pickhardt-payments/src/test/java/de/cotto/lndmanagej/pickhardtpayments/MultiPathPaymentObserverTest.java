package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_CLTV_EXPIRY;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentObserverTest {
    private static final HexString PAYMENT_HASH = DECODED_PAYMENT_REQUEST.paymentHash();
    private static final Duration DURATION = Duration.ofMillis(100);
    private final Executor executor = Executors.newCachedThreadPool();

    @InjectMocks
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    private LiquidityInformationUpdater liquidityInformationUpdater;

    @Test
    void cancels_in_flight_on_error() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        sendToRouteObserver.onError(new NullPointerException());
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(Coins.NONE);
    }

    @Test
    void notifies_about_in_flight_change() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        AtomicBoolean released = new AtomicBoolean(false);
        executor.execute(
                () -> {
                    Duration timeout = Duration.ofSeconds(2);
                    multiPathPaymentObserver.waitForInFlightChange(timeout, PAYMENT_HASH, ROUTE.getAmount());
                    released.set(true);
                }
        );
        sendToRouteObserver.onError(new NullPointerException());
        await().atMost(1, TimeUnit.SECONDS).until(released::get);
    }

    @Test
    @Timeout(1)
    void waitForInFlightChange_returns_without_change_in_timeout() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        multiPathPaymentObserver.waitForInFlightChange(
                Duration.of(100, ChronoUnit.MILLIS),
                PAYMENT_HASH,
                ROUTE.getAmount()
        );
    }

    @Test
    void cancels_in_flight_using_liquidity_information_updater_on_error() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        sendToRouteObserver.onError(new NullPointerException());
        verify(liquidityInformationUpdater).removeInFlight(hops());
    }

    @Test
    void accepts_value() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        assertThatCode(
                () -> sendToRouteObserver.onValue(HexString.EMPTY, FailureCode.UNKNOWN_FAILURE)
        ).doesNotThrowAnyException();
    }

    @Test
    void inFlight_initially_zero() {
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(Coins.NONE);
    }

    @Test
    void inFlight_initialized_with_route_amount() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(ROUTE.getAmount());
    }

    @Test
    void inFlight_reset_on_success() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(new HexString("AABBCC"), FailureCode.UNKNOWN_FAILURE);
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(Coins.NONE);
    }

    @Test
    void isSettled_success() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(new HexString("AABBCC"), FailureCode.UNKNOWN_FAILURE);
        assertThat(multiPathPaymentObserver.isSettled(PAYMENT_HASH)).isTrue();
    }

    @Test
    void isSettled_failure() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(HexString.EMPTY, FailureCode.FEE_INSUFFICIENT);
        assertThat(multiPathPaymentObserver.isSettled(PAYMENT_HASH)).isFalse();
    }

    @Test
    void isSettled_error() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onError(new NullPointerException());
        assertThat(multiPathPaymentObserver.isSettled(PAYMENT_HASH)).isFalse();
    }

    @Test
    void getFailureCode_success() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(new HexString("AABBCC"), FailureCode.UNKNOWN_FAILURE);
        assertThat(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH)).isEmpty();
    }

    @Test
    void getFailureCode_failure_from_final_node() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(HexString.EMPTY, FINAL_INCORRECT_CLTV_EXPIRY);
        assertThat(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH)).contains(FINAL_INCORRECT_CLTV_EXPIRY);
    }

    @Test
    void getFailureCode_failure_from_other_node() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(HexString.EMPTY, FailureCode.PERMANENT_CHANNEL_FAILURE);
        assertThat(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH)).isEmpty();
    }

    @Test
    void getFailureCode_error() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onError(new NullPointerException());
        assertThat(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH)).isEmpty();
    }

    @Test
    void isSettled_initial() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        assertThat(multiPathPaymentObserver.isSettled(PAYMENT_HASH)).isFalse();
    }

    @Test
    void isSettled_unknown() {
        assertThat(multiPathPaymentObserver.isSettled(PAYMENT_HASH)).isFalse();
    }

    @Test
    void inFlight_reset_on_failure() {
        SendToRouteObserver observer = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        observer.onValue(HexString.EMPTY, FailureCode.FEE_INSUFFICIENT);
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(Coins.NONE);
    }

    @Test
    void inFlight_updated_with_several_routes() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        multiPathPaymentObserver.getFor(ROUTE_2, HexString.EMPTY);
        multiPathPaymentObserver.getFor(ROUTE_3, PAYMENT_HASH);
        Coins expectedAmount = ROUTE.getAmount().add(ROUTE_3.getAmount());
        assertThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isEqualTo(expectedAmount);
    }

    @Test
    void waitForInFlightChange_already_changed() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        assumeThat(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).isNotEqualTo(Coins.ofSatoshis(23));
        assertThatCode(
                () -> multiPathPaymentObserver.waitForInFlightChange(DURATION, PAYMENT_HASH, Coins.ofSatoshis(23))
        ).doesNotThrowAnyException();
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
