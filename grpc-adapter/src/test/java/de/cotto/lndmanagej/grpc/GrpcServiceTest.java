package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Meter;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.MetricsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);
    private final MetricsBuilder metricsBuilder = mock(MetricsBuilder.class);
    private TestableGrpcService grpcService;
    private Meter meter;

    @BeforeEach
    void setUp() throws IOException {
        meter = mock(Meter.class);
        when(metricsBuilder.getMetric(any())).thenReturn(meter);
        grpcService = new TestableGrpcService(mock(LndConfiguration.class), metricsBuilder);
    }

    @Test
    void createsMetrics() {
        verify(metricsBuilder, times(5)).getMetric(anyString());
        grpcService.mark("getInfo");
        verify(meter).mark();
    }

    @Test
    void shutdown() {
        grpcService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcService extends GrpcService {
        public TestableGrpcService(
                LndConfiguration lndConfiguration,
                MetricsBuilder metricsBuilder
        ) throws IOException {
            super(lndConfiguration, metricsBuilder);
        }

        @Override
        protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
            return GrpcServiceTest.this.stubCreator;
        }
    }
}