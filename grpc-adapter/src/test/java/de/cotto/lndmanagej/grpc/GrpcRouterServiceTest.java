package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.MetricsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcRouterServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);
    private final MetricsBuilder metricsBuilder = mock(MetricsBuilder.class);
    private TestableGrpcRouterService grpcRouterService;

    @BeforeEach
    void setUp() throws IOException {
        grpcRouterService = new TestableGrpcRouterService(mock(LndConfiguration.class), metricsBuilder);
    }

    @Test
    void createsMetric() {
        verify(metricsBuilder).getMetric(anyString());
    }

    @Test
    void shutdown() {
        grpcRouterService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcRouterService extends GrpcRouterService {
        public TestableGrpcRouterService(
                LndConfiguration lndConfiguration,
                MetricsBuilder metricsBuilder
        ) throws IOException {
            super(lndConfiguration, metricsBuilder);
        }

        @Override
        protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
            return GrpcRouterServiceTest.this.stubCreator;
        }
    }
}