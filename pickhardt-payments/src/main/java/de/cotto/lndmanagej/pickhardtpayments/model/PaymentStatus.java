package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PaymentStatus implements Publisher<InstantWithString> {
    private boolean success;
    private boolean failure;
    private int numberOfAttemptedRoutes;
    private final List<InstantWithString> messages;
    private final List<Subscriber<? super InstantWithString>> subscribers;

    public PaymentStatus() {
        subscribers = Collections.synchronizedList(new ArrayList<>());
        messages = new ArrayList<>();
    }

    public static PaymentStatus createFailure(String reason) {
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.failed(reason);
        return paymentStatus;
    }

    public static PaymentStatus createFor(HexString paymentHash) {
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.info("Initializing payment " + paymentHash);
        return paymentStatus;
    }

    public void settled() {
        synchronized (this) {
            addMessage("Settled");
            success = true;
            subscribers.forEach(Subscriber::onComplete);
        }
    }

    public void failed(FailureCode failureCode) {
        synchronized (this) {
            addMessage("Failed with " + failureCode.toString());
            failure = true;
            subscribers.forEach(Subscriber::onComplete);
        }
    }

    public void failed(String message) {
        synchronized (this) {
            addMessage(message);
            failure = true;
            subscribers.forEach(Subscriber::onComplete);
        }
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

    private void addMessage(String message) {
        InstantWithString messageWithTimestamp = new InstantWithString(message);
        synchronized (this) {
            messages.add(messageWithTimestamp);
            subscribers.forEach(subscriber -> subscriber.onNext(messageWithTimestamp));
        }
    }

    private String getFormattedRoute(Route route) {
        List<String> edgeInformation = route.getEdgesWithLiquidityInformation().stream()
                .map(this::getFormattedEdge)
                .toList();
        return route.getAmount().toStringSat() + ": " + edgeInformation + ", "
               + route.getFeeRate() + "ppm, "
               + route.getFeeRateWithFirstHop() + "ppm with first hop, " +
               "probability " + route.getProbability();
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

    @Override
    public void subscribe(Subscriber<? super InstantWithString> subscriber) {
        subscriber.onSubscribe(new PaymentStatusSubscription(subscriber));
        synchronized (this) {
            messages.forEach(subscriber::onNext);
            subscribers.add(subscriber);
        }
        if (isFailure() || isSuccess()) {
            subscriber.onComplete();
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
               && Objects.equals(messages, that.messages)
               && Objects.equals(subscribers, that.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, failure, numberOfAttemptedRoutes, messages, subscribers);
    }

    private class PaymentStatusSubscription implements Subscription {
        private final Subscriber<? super InstantWithString> subscriber;

        public PaymentStatusSubscription(Subscriber<? super InstantWithString> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long numberOfMessages) {
            // ignore
        }

        @Override
        public void cancel() {
            subscribers.remove(subscriber);
        }
    }
}
