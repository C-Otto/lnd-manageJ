package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class TestableGrpcBase extends GrpcBase {
    public TestableGrpcBase(LndConfiguration lndConfiguration) throws IOException {
        super(lndConfiguration);
    }

    @Override
    protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
        return mock(StubCreator.class);
    }
}
