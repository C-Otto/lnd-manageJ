package de.cotto.lndmanagej.grpc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoopObserverTest {

    private final NoopObserver<String> noopObserver = new NoopObserver<>();

    @Test
    void onNext() {
        assertThatCode(() -> noopObserver.onNext("foo")).doesNotThrowAnyException();
    }

    @Test
    void onCompleted() {
        assertThatCode(noopObserver::onCompleted).doesNotThrowAnyException();
    }

    @Test
    void onError() {
        assertThatCode(() -> noopObserver.onError(new NullPointerException())).doesNotThrowAnyException();
    }
}
