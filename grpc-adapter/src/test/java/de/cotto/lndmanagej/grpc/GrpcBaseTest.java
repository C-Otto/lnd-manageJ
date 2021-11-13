package de.cotto.lndmanagej.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GrpcBaseTest {
    @InjectMocks
    private TestableGrpcBase grpcBase;

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