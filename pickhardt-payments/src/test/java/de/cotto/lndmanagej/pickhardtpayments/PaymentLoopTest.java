package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.InstantWithString;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static de.cotto.lndmanagej.ReactiveStreamReader.readAll;
import static de.cotto.lndmanagej.ReactiveStreamReader.readMessages;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.MAX_RETRIES_AFTER_FAILURE;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.SLEEP_AFTER_FAILURE_MILLISECONDS;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT_2;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLoopTest {
    private static final HexString PAYMENT_HASH = DECODED_PAYMENT_REQUEST.paymentHash();
    private static final PaymentOptions PAYMENT_OPTIONS = PaymentOptions.forFeeRateWeight(123);
    private static final String MPP_1_ROUTE_1 =
            "100: [712345x123x1 (cap 21,000,000), 799999x456x3 (cap 21,000,000), 799999x456x5 (cap 21,000,000)], " +
                    "400ppm, 600ppm with first hop, probability 0.9999857143544217";
    private static final String MPP_1_ROUTE_2 =
            "200: [799999x456x2 (cap 21,000,000), 799999x456x3 (cap 21,000,000)], " +
                    "200ppm, 400ppm with first hop, probability 0.9999809524725624";
    private static final String MPP_2_ROUTE_1 =
            "50: [799999x456x3 (cap 21,000,000), 799999x456x5 (cap 21,000,000)], " +
                    "200ppm, 400ppm with first hop, probability 0.9999952381011338";

    @InjectMocks
    private PaymentLoop paymentLoop;

    @Mock
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Mock
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private GrpcSendToRoute grpcSendToRoute;

    @Mock
    private GrpcInvoices grpcInvoices;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    private PaymentStatus paymentStatus;

    @BeforeEach
    void setUp() {
        lenient().when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH)).thenReturn(Coins.NONE);
        lenient().when(configurationService.getIntegerValue(SLEEP_AFTER_FAILURE_MILLISECONDS))
                .thenReturn(Optional.of(1));
        lenient().when(configurationService.getIntegerValue(MAX_RETRIES_AFTER_FAILURE))
                .thenReturn(Optional.of(0));
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        paymentStatus = PaymentStatus.createFor(PAYMENT_HASH);
    }

    @Test
    void failure_from_splitter() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isFailure()).isTrue();
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .contains("Unable to find route (trying to send 123)");
        softly.assertAll();
        verify(grpcSendToRoute, never()).sendToRoute(any(), any(), any());
        verifyNoInteractions(grpcInvoices);
    }

    @Test
    void fails_after_one_hundred_loop_iterations() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        verify(multiPathPaymentSplitter, times(100)).getMultiPathPaymentTo(any(), any(), any(), anyInt());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isFailure()).isTrue();
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .contains("Failing after 100 loop iterations.");
        softly.assertAll();
        verify(grpcSendToRoute).forceFailureForPayment(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void waits_for_last_iteration_to_succeed() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);
        OngoingStubbing<Boolean> stubbing = when(multiPathPaymentObserver.isSettled(PAYMENT_HASH));
        for (int i = 0; i < 100; i++) {
            stubbing = stubbing.thenReturn(false);
        }
        stubbing.thenReturn(true);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isTrue();
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .doesNotContain("Failing after 100 loop iterations.");
        softly.assertAll();
        verify(multiPathPaymentSplitter, times(100)).getMultiPathPaymentTo(any(), any(), any(), anyInt());
    }

    @Test
    void cancels_invoice_from_own_node() {
        when(grpcGetInfo.getPubkey()).thenReturn(DECODED_PAYMENT_REQUEST.destination());
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        verify(grpcInvoices).cancelPaymentRequest(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void tries_again_after_failure_from_splitter() {
        when(configurationService.getIntegerValue(MAX_RETRIES_AFTER_FAILURE)).thenReturn(Optional.of(1));
        when(configurationService.getIntegerValue(SLEEP_AFTER_FAILURE_MILLISECONDS)).thenReturn(Optional.of(1));
        when(multiPathPaymentObserver.isSettled(PAYMENT_HASH))
                .thenReturn(false)
                .thenReturn(true);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(DECODED_PAYMENT_REQUEST.amount());
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE)
                .thenReturn(MULTI_PATH_PAYMENT);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isFailure()).isFalse();
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .contains("Trying again...");
        softly.assertAll();
    }

    @Test
    void fails_after_configured_number_of_retry_attempts() {
        when(configurationService.getIntegerValue(MAX_RETRIES_AFTER_FAILURE)).thenReturn(Optional.of(1));
        when(configurationService.getIntegerValue(SLEEP_AFTER_FAILURE_MILLISECONDS)).thenReturn(Optional.of(1));
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                any(),
                any(),
                anyInt()
        )).thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        assertThat(paymentStatus.isFailure()).isTrue();
        verify(multiPathPaymentSplitter, times(2)).getMultiPathPaymentTo(any(), any(), any(), anyInt());
        verify(grpcSendToRoute).forceFailureForPayment(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void failure_with_information() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.failure("something"));
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);

        assertThat(paymentStatus.isFailure()).isTrue();
        assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .contains("Unable to find route (trying to send 123): something");
        verify(grpcSendToRoute, never()).sendToRoute(any(), any(), any());
        verify(grpcSendToRoute).forceFailureForPayment(any());
    }

    @Test
    void requests_route_with_expected_parameters() {
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        verify(multiPathPaymentSplitter).getMultiPathPaymentTo(
                DECODED_PAYMENT_REQUEST.destination(),
                DECODED_PAYMENT_REQUEST.amount(),
                PAYMENT_OPTIONS,
                DECODED_PAYMENT_REQUEST.cltvExpiry());
    }

    @Test
    void does_not_cancel_settled_invoice() {
        when(grpcGetInfo.getPubkey()).thenReturn(DECODED_PAYMENT_REQUEST.destination());
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        verify(grpcInvoices, never()).cancelPaymentRequest(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void does_not_fail_settled_payment() {
        when(grpcGetInfo.getPubkey()).thenReturn(DECODED_PAYMENT_REQUEST.destination());
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        verify(grpcSendToRoute, never()).forceFailureForPayment(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void sends_to_each_route() {
        mockSuccessOnFirstAttempt();
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            verify(grpcSendToRoute).sendToRoute(eq(route), eq(DECODED_PAYMENT_REQUEST), any());
        }
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isTrue();
        softly.assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(MULTI_PATH_PAYMENT.routes().size());
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string).containsExactly(
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
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                any(),
                any(),
                anyInt())
        ).thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(DECODED_PAYMENT_REQUEST.amount());
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        // it's not strictly a failure, but we need to complete the flux/stream (see #88)
        assertThat(paymentStatus.isFailure()).isTrue();
        verify(grpcSendToRoute).forceFailureForPayment(DECODED_PAYMENT_REQUEST);
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

        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
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
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), eq(amountForSecondAttempt), any(), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT_2);

        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        for (Route route : MULTI_PATH_PAYMENT_2.routes()) {
            verify(grpcSendToRoute).sendToRoute(eq(route), eq(DECODED_PAYMENT_REQUEST), any());
        }
        int numberOfRoutes = MULTI_PATH_PAYMENT.routes().size() + MULTI_PATH_PAYMENT_2.routes().size();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isTrue();
        softly.assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(numberOfRoutes);
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .containsExactly(
                "Initializing payment " + PAYMENT_HASH,
                "#1: Sending 123 (0.0% = 0 in flight)",
                "Sending to route #1: " + MPP_1_ROUTE_1,
                "Sending to route #2: " + MPP_1_ROUTE_2,
                "#2: Sending 3 (97.2% = 120 in flight)",
                "Sending to route #3: " + MPP_2_ROUTE_1,
                "Settled"
        );
        softly.assertAll();
    }

    @Test
    void fails_after_waiting_for_shard_failure_or_settled_payment() {
        Coins totalAmountToSend = DECODED_PAYMENT_REQUEST.amount();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                eq(DECODED_PAYMENT_REQUEST.amount()),
                any(),
                anyInt())
        ).thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(Coins.NONE)
                .thenReturn(totalAmountToSend)
                .thenReturn(totalAmountToSend);

        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        verify(multiPathPaymentObserver).waitForInFlightChange(any(), eq(PAYMENT_HASH), eq(totalAmountToSend));
        assertThat(readMessages(paymentStatus, 8)).map(InstantWithString::string)
                .contains("Stopping payment loop, full amount is in-flight, but no failure/settle message received " +
                        "within timeout. The payment might settle/fail in the future.");
        verify(grpcSendToRoute).forceFailureForPayment(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void aborts_on_failure_from_destination_node() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                eq(DECODED_PAYMENT_REQUEST.amount()),
                any(),
                anyInt()
        )).thenReturn(MULTI_PATH_PAYMENT);

        when(multiPathPaymentObserver.getFailureCode(PAYMENT_HASH))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(FailureCode.MPP_TIMEOUT));

        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        verify(multiPathPaymentSplitter, times(1)).getMultiPathPaymentTo(any(), any(), any(), anyInt());
        assertThat(paymentStatus.isFailure()).isTrue();
    }

    @Test
    void aborts_if_no_route_can_be_computed() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                eq(DECODED_PAYMENT_REQUEST.amount()),
                any(),
                anyInt()
        )).thenReturn(MultiPathPayment.FAILURE);
        paymentLoop.start(DECODED_PAYMENT_REQUEST, PAYMENT_OPTIONS, paymentStatus);
        assertThat(paymentStatus.isFailure()).isTrue();
    }

    // CPD-OFF
    private void mockSuccessOnFirstAttempt() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), any(), anyInt()))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentObserver.isSettled(PAYMENT_HASH))
                .thenReturn(false)
                .thenReturn(true);
        when(multiPathPaymentObserver.getInFlight(PAYMENT_HASH))
                .thenReturn(Coins.NONE)
                .thenReturn(DECODED_PAYMENT_REQUEST.amount());
    }

    private void mockPartialFailureInFirstAttempt(Coins pendingAfterFirstAttempt) {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                any(),
                eq(DECODED_PAYMENT_REQUEST.amount()),
                any(),
                anyInt()
        )).thenReturn(MULTI_PATH_PAYMENT);
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
