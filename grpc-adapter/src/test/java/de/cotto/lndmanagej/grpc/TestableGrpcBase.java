package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.configuration.ConfigurationService;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class TestableGrpcBase extends GrpcBase {
    public TestableGrpcBase(
            ConfigurationService configurationService,
            String homeDirectory
    ) throws IOException {
        super(configurationService, homeDirectory);
    }

    @Override
    protected StubCreator getStubCreator() {
        return mock(StubCreator.class);
    }
}
