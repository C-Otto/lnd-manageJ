package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.service.LiquidityInformationUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentObserverIT {
    private static final HexString PAYMENT_HASH = DECODED_PAYMENT_REQUEST.paymentHash();
    private static final Duration DURATION = Duration.ofSeconds(1);
    private static final Coins IN_FLIGHT = Coins.ofSatoshis(100);

    @InjectMocks
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    @SuppressWarnings("unused")
    private LiquidityInformationUpdater liquidityInformationUpdater;

    private final Executor executor = Executors.newCachedThreadPool();

    @Test
    @Timeout(30)
    void waitForInFlightChange_stuck() {
        multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        assertThatCode(
                () -> multiPathPaymentObserver.waitForInFlightChange(DURATION, PAYMENT_HASH, IN_FLIGHT)
        ).doesNotThrowAnyException();
    }

    @Test
    @Timeout(value = 900, unit = TimeUnit.MILLISECONDS)
    void waitForInFlightChange_changed_from_other_thread() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        executor.execute(() -> unlockAfterSomeMilliSeconds(sendToRouteObserver));

        assertThatCode(
                () -> multiPathPaymentObserver.waitForInFlightChange(DURATION, PAYMENT_HASH, IN_FLIGHT)
        ).doesNotThrowAnyException();
    }

    @Test
    @Timeout(value = 900, unit = TimeUnit.MILLISECONDS)
    void waitForInFlightChange_two_waiting_changed_from_other_thread() {
        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(ROUTE, PAYMENT_HASH);
        executor.execute(() -> unlockAfterSomeMilliSeconds(sendToRouteObserver));
        executor.execute(() -> multiPathPaymentObserver.waitForInFlightChange(DURATION, PAYMENT_HASH, IN_FLIGHT));

        assertThatCode(
                () -> multiPathPaymentObserver.waitForInFlightChange(DURATION, PAYMENT_HASH, IN_FLIGHT)
        ).doesNotThrowAnyException();
    }

    private void unlockAfterSomeMilliSeconds(SendToRouteObserver sendToRouteObserver) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // ignore
        }
        sendToRouteObserver.onError(new NullPointerException());
    }
}
