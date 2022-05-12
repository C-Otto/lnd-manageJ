package de.cotto.lndmanagej.grpc;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReportingStreamObserverTest {

    private final MySendToRouteObserver sendToRouteObserver = new MySendToRouteObserver();
    private final ReportingStreamObserver<String> reportingStreamObserver =
            new ReportingStreamObserver<>(sendToRouteObserver);

    @Test
    void onNext() {
        assertThatCode(() -> reportingStreamObserver.onNext("foo")).doesNotThrowAnyException();
    }

    @Test
    void onCompleted() {
        assertThatCode(reportingStreamObserver::onCompleted).doesNotThrowAnyException();
    }

    @Test
    void onError() {
        NullPointerException throwable = new NullPointerException();
        assertThatCode(() -> reportingStreamObserver.onError(throwable)).doesNotThrowAnyException();
        assertThat(sendToRouteObserver.seenThrowable).isSameAs(throwable);
    }

    @Test
    void onValue() {
        String value = "";
        assertThatCode(() -> reportingStreamObserver.onNext(value)).doesNotThrowAnyException();
        assertThat(sendToRouteObserver.seenValue).isSameAs(value);
    }

    private static class MySendToRouteObserver implements SendToRouteObserver {
        @Nullable
        private Throwable seenThrowable;

        @Nullable
        private Object seenValue;

        @Override
        public void onError(Throwable throwable) {
            seenThrowable = throwable;
        }

        @Override
        public void onValue(Object value) {
            seenValue = value;
        }
    }
}
