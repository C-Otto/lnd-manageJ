package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoopSendToRouteObserverTest {
    private final NoopSendToRouteObserver noopSendToRouteObserver = new NoopSendToRouteObserver();

    @Test
    void does_nothing_on_error() {
        assertThatCode(
                () -> noopSendToRouteObserver.onError(new NullPointerException())
        ).doesNotThrowAnyException();
    }

    @Test
    void does_nothing_on_value() {
        assertThatCode(
                () -> noopSendToRouteObserver.onValue(HexString.EMPTY, FailureCode.UNKNOWN_FAILURE)
        ).doesNotThrowAnyException();
    }
}
