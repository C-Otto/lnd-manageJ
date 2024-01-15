package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.MAX_RETRIES_AFTER_FAILURE;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.SLEEP_AFTER_FAILURE_MILLISECONDS;

@Component
public class PaymentLoop {
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_SLEEP_MILLISECONDS = 500;
    private static final int MAX_NUMBER_OF_LOOPS = 100;

    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    private static final String TIMEOUT_MESSAGE =
            "Stopping payment loop, full amount is in-flight, but no failure/settle message received within timeout. " +
                    "The payment might settle/fail in the future.";

    private final MultiPathPaymentObserver multiPathPaymentObserver;
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;
    private final ConfigurationService configurationService;
    private final GrpcSendToRoute grpcSendToRoute;
    private final GrpcInvoices grpcInvoices;
    private final GrpcGetInfo grpcGetInfo;

    public PaymentLoop(
            MultiPathPaymentObserver multiPathPaymentObserver,
            MultiPathPaymentSplitter multiPathPaymentSplitter,
            ConfigurationService configurationService, GrpcSendToRoute grpcSendToRoute,
            GrpcInvoices grpcInvoices,
            GrpcGetInfo grpcGetInfo
    ) {
        this.multiPathPaymentObserver = multiPathPaymentObserver;
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
        this.configurationService = configurationService;
        this.grpcSendToRoute = grpcSendToRoute;
        this.grpcInvoices = grpcInvoices;
        this.grpcGetInfo = grpcGetInfo;
    }

    @Async
    public void start(
            DecodedPaymentRequest decodedPaymentRequest,
            PaymentOptions paymentOptions,
            PaymentStatus paymentStatus
    ) {
        new Instance(decodedPaymentRequest, paymentOptions, paymentStatus).start();
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        if (paymentStatus.isFailure() && decodedPaymentRequest.destination().equals(ownPubkey)) {
            grpcInvoices.cancelPaymentRequest(decodedPaymentRequest);
        }
        if (paymentStatus.isPending() || paymentStatus.isFailure()) {
            grpcSendToRoute.forceFailureForPayment(decodedPaymentRequest);
        }
    }

    private class Instance {
        private final DecodedPaymentRequest decodedPaymentRequest;
        private final PaymentOptions paymentOptions;
        private final PaymentStatus paymentStatus;
        private final HexString paymentHash;
        private final Pubkey destination;
        private final Coins totalAmountToSend;
        private final int finalCltvDelta;
        private Coins inFlight = Coins.NONE;

        public Instance(
                DecodedPaymentRequest decodedPaymentRequest,
                PaymentOptions paymentOptions,
                PaymentStatus paymentStatus
        ) {
            this.decodedPaymentRequest = decodedPaymentRequest;
            this.paymentOptions = paymentOptions;
            this.paymentStatus = paymentStatus;
            paymentHash = decodedPaymentRequest.paymentHash();
            destination = decodedPaymentRequest.destination();
            totalAmountToSend = decodedPaymentRequest.amount();
            finalCltvDelta = decodedPaymentRequest.cltvExpiry();
        }

        private void start() {
            int loopIterationCounter = 0;
            int failureCounter = 0;
            while (shouldContinue()) {
                loopIterationCounter++;
                Coins residualAmount = totalAmountToSend.subtract(inFlight);
                if (Coins.NONE.equals(residualAmount)) {
                    paymentStatus.info(TIMEOUT_MESSAGE);
                    return;
                }
                if (shouldAbort(loopIterationCounter)) {
                    paymentStatus.failed("Failing after " + MAX_NUMBER_OF_LOOPS + " loop iterations.");
                    return;
                }
                addLoopIterationInfo(loopIterationCounter, residualAmount);
                MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPaymentTo(
                        destination,
                        residualAmount,
                        paymentOptions,
                        finalCltvDelta
                );
                if (multiPathPayment.isFailure()) {
                    failureCounter++;
                    logFailureInformation(residualAmount, multiPathPayment);
                    if (failureCounter > getMaxRetriesAfterFailure()) {
                        paymentStatus.failed("Giving up after " + failureCounter + " failed attempts to compute route");
                        return;
                    }
                    paymentStatus.info("Trying again...");
                    sleepAfterFailure();
                } else {
                    failureCounter = 0;
                    List<Route> routes = multiPathPayment.routes();
                    for (Route route : routes) {
                        SendToRouteObserver sendToRouteObserver = multiPathPaymentObserver.getFor(route, paymentHash);
                        grpcSendToRoute.sendToRoute(route, decodedPaymentRequest, sendToRouteObserver);
                        paymentStatus.sending(route);
                    }
                }
            }
        }

        private void logFailureInformation(Coins residualAmount, MultiPathPayment multiPathPayment) {
            String information = multiPathPayment.information();
            if (Strings.isNotEmpty(information)) {
                paymentStatus.info(
                        "Unable to find route (trying to send %s): %s"
                                .formatted(residualAmount.toStringSat(), information)
                );
            } else {
                paymentStatus.info(
                        "Unable to find route (trying to send %s)"
                                .formatted(residualAmount.toStringSat())
                );
            }
        }

        private void sleepAfterFailure() {
            try {
                Thread.sleep(getSleepAfterFailureMillis());
            } catch (InterruptedException ignored) {
                // ignore
            }
        }

        private void addLoopIterationInfo(int loopCounter, Coins residualAmount) {
            long inFlightMilliSat = inFlight.milliSatoshis();
            long totalMilliSat = totalAmountToSend.milliSatoshis();
            double percentageInFlight = (int) (1000.0 * inFlightMilliSat / totalMilliSat) / 10.0;
            paymentStatus.info("#%d: Sending %s (%s%% = %s in flight)".formatted(
                    loopCounter, residualAmount.toStringSat(), percentageInFlight, inFlight.toStringSat())
            );
        }

        private boolean shouldContinue() {
            updateInformation();
            if (!paymentStatus.isPending()) {
                return false;
            }
            boolean fullAmountInFlight = inFlight.equals(decodedPaymentRequest.amount());
            if (fullAmountInFlight) {
                multiPathPaymentObserver.waitForInFlightChange(TIMEOUT, paymentHash, decodedPaymentRequest.amount());
                updateInformation();
            }
            return paymentStatus.isPending();
        }

        private boolean shouldAbort(int loopIterationCounter) {
            return paymentStatus.isPending() && loopIterationCounter > MAX_NUMBER_OF_LOOPS;
        }

        private void updateInformation() {
            inFlight = multiPathPaymentObserver.getInFlight(paymentHash);
            if (multiPathPaymentObserver.isSettled(paymentHash)) {
                paymentStatus.settled();
            }
            multiPathPaymentObserver.getFailureCode(paymentHash).ifPresent(paymentStatus::failed);
        }
    }

    private int getSleepAfterFailureMillis() {
        return configurationService.getIntegerValue(SLEEP_AFTER_FAILURE_MILLISECONDS)
                .orElse(DEFAULT_SLEEP_MILLISECONDS);
    }

    private int getMaxRetriesAfterFailure() {
        return configurationService.getIntegerValue(MAX_RETRIES_AFTER_FAILURE).orElse(DEFAULT_MAX_RETRIES);
    }
}
