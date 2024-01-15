package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PaymentStatus extends Flux<InstantWithString> {
    private boolean success;
    private boolean failure;
    private int numberOfAttemptedRoutes;
    private final List<InstantWithString> allMessages;
    private final List<PaymentStatusSubscription> subscriptions;

    public PaymentStatus() {
        super();
        subscriptions = Collections.synchronizedList(new ArrayList<>());
        allMessages = new ArrayList<>();
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
            subscriptions.forEach(PaymentStatusSubscription::onComplete);
        }
    }

    public void failed(FailureCode failureCode) {
        synchronized (this) {
            addMessage("Failed with " + failureCode.toString());
            failure = true;
            subscriptions.forEach(PaymentStatusSubscription::onComplete);
        }
    }

    public void failed(String message) {
        synchronized (this) {
            addMessage(message);
            failure = true;
            subscriptions.forEach(PaymentStatusSubscription::onComplete);
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
            allMessages.add(messageWithTimestamp);
            subscriptions.forEach(paymentStatusSubscription -> paymentStatusSubscription.onNext(messageWithTimestamp));
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
    public void subscribe(@Nonnull CoreSubscriber<? super InstantWithString> subscriber) {
        PaymentStatusSubscription subscription =
                new PaymentStatusSubscription(subscriber, new ArrayList<>(allMessages));
        synchronized (this) {
            subscriber.onSubscribe(subscription);
            subscriptions.add(subscription);
        }
        if (isFailure() || isSuccess()) {
            subscription.onComplete();
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
               && Objects.equals(allMessages, that.allMessages)
               && Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, failure, numberOfAttemptedRoutes, allMessages, subscriptions);
    }

    private class PaymentStatusSubscription implements Subscription {
        private final Subscriber<? super InstantWithString> subscriber;
        private final List<InstantWithString> messagesForSubscriber;
        private long requested;

        public PaymentStatusSubscription(
                Subscriber<? super InstantWithString> subscriber,
                List<InstantWithString> messages
        ) {
            this.subscriber = subscriber;
            messagesForSubscriber = messages;
        }

        @Override
        public void request(long numberOfMessages) {
            synchronized (this) {
                requested += numberOfMessages;
                sendRequestedMessages();
            }
        }

        @Override
        public void cancel() {
            subscriptions.remove(this);
        }

        public void onComplete() {
            subscriber.onComplete();
        }

        public void onNext(InstantWithString message) {
            synchronized (this) {
                messagesForSubscriber.add(message);
                sendRequestedMessages();
            }
        }

        private void sendRequestedMessages() {
            while (requested > 0 && !messagesForSubscriber.isEmpty()) {
                subscriber.onNext(messagesForSubscriber.get(0));
                messagesForSubscriber.remove(0);
                requested--;
            }
        }
    }
}
