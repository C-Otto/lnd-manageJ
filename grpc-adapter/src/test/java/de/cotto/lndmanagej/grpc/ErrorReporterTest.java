package de.cotto.lndmanagej.grpc;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ErrorReporterTest {

    private final ErrorReporter<String> errorReporter = new ErrorReporter<>(this::consumeThrowable);

    @Nullable
    private Throwable seenThrowable;

    @Test
    void onNext() {
        assertThatCode(() -> errorReporter.onNext("foo")).doesNotThrowAnyException();
    }

    @Test
    void onCompleted() {
        assertThatCode(errorReporter::onCompleted).doesNotThrowAnyException();
    }

    @Test
    void onError() {
        NullPointerException throwable = new NullPointerException();
        assertThatCode(() -> errorReporter.onError(throwable)).doesNotThrowAnyException();
        assertThat(seenThrowable).isSameAs(throwable);
    }

    private void consumeThrowable(Throwable throwable) {
        seenThrowable = throwable;
    }
}
