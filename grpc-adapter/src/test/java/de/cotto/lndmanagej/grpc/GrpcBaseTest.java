package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Timer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GrpcBaseTest {
    @InjectMocks
    private TestableGrpcBase grpcBase;

    @BeforeEach
    void setUp() {
        Timer timer = mock(Timer.class);
        lenient().when(timer.timeSupplier(any())).then(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void get() {
        assertThat(grpcBase.get(() -> "x")).contains("x");
    }

    @Test
    void get_failure() {
        assertThat(grpcBase.get(() -> {
            throw new StatusRuntimeException(Status.ABORTED);
        })).isEmpty();
    }
}