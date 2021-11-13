package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);
    private final Metrics metrics = mock(Metrics.class);
    private TestableGrpcService grpcService;

    @BeforeEach
    void setUp() throws IOException {
        grpcService = new TestableGrpcService(mock(LndConfiguration.class), metrics);
    }

    @Test
    void shutdown() {
        grpcService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcService extends GrpcService {
        public TestableGrpcService(
                LndConfiguration lndConfiguration,
                Metrics metrics
        ) throws IOException {
            super(lndConfiguration, metrics);
        }

        @Override
        protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
            return GrpcServiceTest.this.stubCreator;
        }
    }
}