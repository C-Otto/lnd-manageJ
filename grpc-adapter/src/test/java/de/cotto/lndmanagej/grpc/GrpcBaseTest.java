package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.metrics.Metrics;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcBaseTest {
    @InjectMocks
    private TestableGrpcBase grpcBase;

    @Mock
    private Metrics metrics;

    @Test
    void get() {
        assertThat(grpcBase.get(() -> "x")).contains("x");
    }

    @Test
    void name() {
        grpcBase.mark("foo");
        verify(metrics).mark(argThat(name -> name.endsWith(".foo")));
    }

    @Test
    void get_failure() {
        assertThat(grpcBase.get(() -> {
            throw new StatusRuntimeException(Status.ABORTED);
        })).isEmpty();
    }
}