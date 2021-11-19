package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class TestableGrpcBase extends GrpcBase {
    public TestableGrpcBase(LndConfiguration lndConfiguration, Metrics metrics) throws IOException {
        super(lndConfiguration, metrics);
    }

    @Override
    protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
        return mock(StubCreator.class);
    }
}
