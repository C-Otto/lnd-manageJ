package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus.InstantWithString;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT_2;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLoopTest {
    private static final HexString PAYMENT_HASH = DECODED_PAYMENT_REQUEST.paymentHash();
    private static final int FEE_RATE_WEIGHT = 123;
    private static final String MPP_1_ROUTE_1 =
            "100: [712345x123x1 (cap 21,000,000), 799999x456x3 (cap 21,000,000), 799999x456x5 (cap 21,000,000)], " +
                    "400ppm, probability 0.9999857143544217";
    private static final String MPP_1_ROUTE_2 =
            "200: [799999x456x2 (cap 21,000,000), 799999x456x3 (cap 21,000,000)], " +
                    "200ppm, probability 0.9999809524725624";
    private static final String MPP_2_ROUTE_1 =
            "[799999x456x3 (cap 21,000,000), 799999x456x5 (cap 21,000,000)], " +
                    "200ppm, probability 0.9999952381011338";

    @InjectMocks
    private PaymentLoop paymentLoop;

    @Mock
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private GrpcSendToRoute grpcSendToRoute;

    private PaymentStatus paymentStatus;

    @BeforeEach
    void setUp() {
        lenient().when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).thenReturn(Coins.NONE);
        paymentStatus = new PaymentStatus(PAYMENT_HASH);
    }

    @Test
    void failure_from_splitter() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isFailure()).isTrue();
        softly.assertThat(paymentStatus.getMessages().stream().map(InstantWithString::string)).contains("");
        verifyNoInteractions(grpcSendToRoute);
    }

    @Test
    void requests_route_with_expected_parameters() {
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        verify(multiPathPaymentSplitter).getMultiPathPaymentTo(
                DECODED_PAYMENT_REQUEST.destination(),
                DECODED_PAYMENT_REQUEST.amount(),
                FEE_RATE_WEIGHT
        );
    }

    @Test
    void sends_to_each_route() {
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            verify(grpcSendToRoute).sendToRoute(eq(route), eq(DECODED_PAYMENT_REQUEST), any());
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isTrue();
        softly.assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(MULTI_PATH_PAYMENT.routes().size());
        softly.assertThat(paymentStatus.getMessages().stream().map(InstantWithString::string)).containsExactly(
                "Initializing payment " + PAYMENT_HASH,
                "#1: Sending 123 (0.0% = 0 in flight)",
                "Sending to route #1: " + MPP_1_ROUTE_1,
                "Sending to route #2: " + MPP_1_ROUTE_2,
                "Settled"
        );
        softly.assertAll();
    }

    @Test
    void aborts_for_stuck_pending_amount() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), anyInt())).thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(DECODED_PAYMENT_REQUEST.amount());
        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isFalse();
        softly.assertThat(paymentStatus.isFailure()).isFalse();
        softly.assertAll();
    }

    @Test
    void registers_observers_for_routes() {
        mockSuccessOnFirstAttempt();

        Map<Route, SendToRouteObserver> expected = new LinkedHashMap<>();
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            SendToRouteObserver expectedObserver = mock(SendToRouteObserver.class);
            when(multiPathPaymentObserver.getFor(route, PAYMENT_HASH)).thenReturn(expectedObserver);
            expected.put(route, expectedObserver);
        }

        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            verify(grpcSendToRoute).sendToRoute(route, DECODED_PAYMENT_REQUEST, requireNonNull(expected.get(route)));
        }
    }

    @Test
    void attempts_second_mpp_on_failure() {
        Coins totalAmountToSend = DECODED_PAYMENT_REQUEST.amount();
        Coins pendingAfterFirstAttempt = Coins.ofSatoshis(120);
        mockPartialFailureInFirstAttempt(pendingAfterFirstAttempt);

        Coins amountForSecondAttempt = totalAmountToSend.subtract(pendingAfterFirstAttempt);
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(amountForSecondAttempt), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT_2);

        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        for (Route route : MULTI_PATH_PAYMENT_2.routes()) {
            verify(grpcSendToRoute).sendToRoute(eq(route), eq(DECODED_PAYMENT_REQUEST), any());
        }
        int numberOfRoutes = MULTI_PATH_PAYMENT.routes().size() + MULTI_PATH_PAYMENT_2.routes().size();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isTrue();
        softly.assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(numberOfRoutes);
        softly.assertThat(paymentStatus.getMessages().stream().map(InstantWithString::string)).containsExactly(
                "Initializing payment " + PAYMENT_HASH,
                "#1: Sending 123 (0.0% = 0 in flight)",
                "Sending to route #1: " + MPP_1_ROUTE_1,
                "Sending to route #2: " + MPP_1_ROUTE_2,
                "#2: Sending 3 (97.2% = 120 in flight)",
                "Sending to route #3: 50: " + MPP_2_ROUTE_1,
                "Settled"
        );
        softly.assertAll();
    }

    @Test
    void fails_after_waiting_for_shard_failure_or_settled_payment() {
        Coins totalAmountToSend = DECODED_PAYMENT_REQUEST.amount();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(DECODED_PAYMENT_REQUEST.amount()), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(Coins.NONE)
                .thenReturn(totalAmountToSend)
                .thenReturn(totalAmountToSend);

        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        verify(multiPathPaymentObserver).waitForInFlightChange(any(), eq(PAYMENT_HASH), eq(totalAmountToSend));
        assertThat(paymentStatus.getMessages().stream().map(InstantWithString::string))
                .contains("Stopping payment loop, full amount is in-flight, but no failure/settle message received " +
                        "within timeout. The payment might settle/fail in the future.");
    }

    @Test
    void aborts_on_failure_from_destination_node() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(DECODED_PAYMENT_REQUEST.amount()), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);

        when(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(FailureCode.MPP_TIMEOUT));

        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        verify(multiPathPaymentSplitter, times(1)).getMultiPathPaymentTo(any(), any(), anyInt());
        assertThat(paymentStatus.isFailure()).isTrue();
    }

    @Test
    void aborts_if_no_route_can_be_computed() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(DECODED_PAYMENT_REQUEST.amount()), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, FEE_RATE_WEIGHT, paymentStatus);
        assertThat(paymentStatus.isFailure()).isTrue();
    }

    // CPD-OFF
    private void mockSuccessOnFirstAttempt() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), anyInt())).thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.isSettled(PAYMENT_HASH))
                .thenReturn(false)
                .thenReturn(true);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(DECODED_PAYMENT_REQUEST.amount());
    }

    private void mockPartialFailureInFirstAttempt(Coins pendingAfterFirstAttempt) {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(DECODED_PAYMENT_REQUEST.amount()), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.isSettled(PAYMENT_HASH))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(pendingAfterFirstAttempt);
    }
    // CPD-ON
}
