package de.cotto.lndmanagej.pickhardtpayments.model;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaymentStatus {
    private boolean success;
    private boolean failure;
    private int numberOfAttemptedRoutes;
    private final List<InstantWithString> messages = new ArrayList<>();

    public PaymentStatus(HexString paymentHash) {
        if (!paymentHash.equals(HexString.EMPTY)) {
            info("Initializing payment " + paymentHash);
        }
    }

    public static PaymentStatus createFailure(String reason) {
        PaymentStatus paymentStatus = new PaymentStatus(HexString.EMPTY);
        paymentStatus.failed(reason);
        return paymentStatus;
    }

    public void settled() {
        success = true;
        addMessage("Settled");

    }

    public void failed(FailureCode failureCode) {
        failure = true;
        addMessage("Failed with " + failureCode.toString());
    }

    public void failed(String message) {
        failure = true;
        addMessage(message);
    }

    public void info(String message) {
        addMessage(message);
    }

    public void sending(Route route) {
        numberOfAttemptedRoutes++;
        String formattedRoute = getFormattedRoute(route);
        addMessage("Sending to route #%d: %s".formatted(numberOfAttemptedRoutes, formattedRoute));
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return failure;
    }

    public boolean isPending() {
        return !success && !failure;
    }

    public int getNumberOfAttemptedRoutes() {
        return numberOfAttemptedRoutes;
    }

    public List<InstantWithString> getMessages() {
        return messages;
    }

    private void addMessage(String message) {
        messages.add(new InstantWithString(message));
    }

    private String getFormattedRoute(Route route) {
        List<String> edgeInformation = route.getEdgesWithLiquidityInformation().stream()
                .map(this::getFormattedEdge)
                .toList();
        return route.getAmount().toStringSat() + ": " + edgeInformation + ", "
                + route.getFeeRate() + "ppm, probability " + route.getProbability();
    }

    private String getFormattedEdge(EdgeWithLiquidityInformation edge) {
        StringBuilder stringBuilder = new StringBuilder(edge.channelId().toString()).append(" (");
        Coins lowerBound = edge.availableLiquidityLowerBound();
        Coins upperBound = edge.availableLiquidityUpperBound();
        Coins capacity = edge.capacity();
        if (lowerBound.equals(upperBound)) {
            stringBuilder.append("known ").append(lowerBound.toStringSat());
        } else {
            if (lowerBound.isPositive()) {
                stringBuilder.append("min ").append(lowerBound.toStringSat()).append(", ");
            }
            if (!upperBound.equals(capacity)) {
                stringBuilder.append("max ").append(upperBound.toStringSat()).append(", ");
            }
            stringBuilder.append("cap ").append(capacity.toStringSat());
        }
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    @VisibleForTesting
    public record InstantWithString(Instant instant, String string) {
        InstantWithString(String string) {
            this(Instant.now(), string);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PaymentStatus that = (PaymentStatus) other;
        return success == that.success
                && failure == that.failure
                && numberOfAttemptedRoutes == that.numberOfAttemptedRoutes
                && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, failure, numberOfAttemptedRoutes, messages);
    }
}
