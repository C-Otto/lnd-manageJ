package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Timer;
import de.cotto.lndmanagej.metrics.Metrics;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcBaseTest {
    @InjectMocks
    private TestableGrpcBase grpcBase;

    @Mock
    private Metrics metrics;

    @BeforeEach
    void setUp() {
        Timer timer = mock(Timer.class);
        lenient().when(timer.timeSupplier(any())).then(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
        lenient().when(metrics.timer(anyString())).thenReturn(timer);
    }

    @Test
    void get() {
        assertThat(grpcBase.get("", () -> "x")).contains("x");
    }

    @Test
    void get_uses_timer() {
        grpcBase.get("name", () -> "x");
        verify(metrics).timer("name");
    }

    @Test
    void name() {
        grpcBase.mark("foo");
        verify(metrics).mark(argThat(name -> name.endsWith(".foo")));
    }

    @Test
    void get_failure() {
        assertThat(grpcBase.get("", () -> {
            throw new StatusRuntimeException(Status.ABORTED);
        })).isEmpty();
    }
}