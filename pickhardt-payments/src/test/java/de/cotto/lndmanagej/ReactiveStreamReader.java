package de.cotto.lndmanagej;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ReactiveStreamReader<T> implements Subscriber<T> {
    private final List<T> messages = new ArrayList<>();

    @Nullable
    private final Integer expectedMessages;

    private boolean done;

    @CheckForNull
    private Subscription subscription;

    public ReactiveStreamReader(int expectedMessages) {
        this.expectedMessages = expectedMessages;
    }

    @SuppressWarnings("PMD.NullAssignment")
    public ReactiveStreamReader() {
        this.expectedMessages = null;
    }

    public static <T> List<T> readMessages(Publisher<T> publisher, int expectedMessages) {
        ReactiveStreamReader<T> instance = new ReactiveStreamReader<>(expectedMessages);
        publisher.subscribe(instance);
        return instance.getMessages();
    }

    public static <T> List<T> readAll(Publisher<T> publisher) {
        ReactiveStreamReader<T> instance = new ReactiveStreamReader<>();
        publisher.subscribe(instance);
        return instance.getMessages();
    }

    private List<T> getMessages() {
        await().atMost(2, SECONDS).until(() -> done);
        if (expectedMessages != null) {
            return messages.subList(0, expectedMessages);
        }
        return messages;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T message) {
        messages.add(message);
        if (expectedMessages != null && messages.size() == expectedMessages) {
            done = true;
        } else {
            Objects.requireNonNull(subscription).request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        done = true;
    }

    @Override
    public void onComplete() {
        done = true;
    }
}
