package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcHtlcEvents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.ForwardFailureFixtures.FORWARD_FAILURE;
import static de.cotto.lndmanagej.model.SettledForwardFixtures.SETTLED_FORWARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.info;

@ExtendWith(MockitoExtension.class)
class HtlcLoggerTest {
    private final TestLogger logger = TestLoggerFactory.getTestLogger(HtlcLogger.class);

    @InjectMocks
    private HtlcLogger htlcLogger;

    @Mock
    @SuppressWarnings("unused")
    private GrpcHtlcEvents grpcHtlcEvents;

    @Test
    void logSettledForwards() {
        when(grpcHtlcEvents.getSettledForwards()).thenReturn(Stream.of(SETTLED_FORWARD));
        htlcLogger.logSettledForwards();
        assertThat(logger.getLoggingEvents()).contains(info("Settled Forward: {}", SETTLED_FORWARD));
    }

    @Test
    void logForwardFailures() {
        when(grpcHtlcEvents.getForwardFailures()).thenReturn(Stream.of(FORWARD_FAILURE));
        htlcLogger.logForwardFailures();
        assertThat(logger.getLoggingEvents()).contains(info("Forward Failure: {}", FORWARD_FAILURE));
    }
}