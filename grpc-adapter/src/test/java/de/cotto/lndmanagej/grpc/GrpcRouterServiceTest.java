package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcRouterServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);
    private TestableGrpcRouterService grpcRouterService;

    @Mock
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() throws IOException {
        grpcRouterService = new TestableGrpcRouterService(configurationService, "/home/foo");
    }

    @Test
    void shutdown() {
        grpcRouterService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcRouterService extends GrpcRouterService {
        public TestableGrpcRouterService(
                ConfigurationService configurationService,
                String homeDirectory
        ) throws IOException {
            super(configurationService, homeDirectory);
        }

        @Override
        protected StubCreator getStubCreator() {
            return GrpcRouterServiceTest.this.stubCreator;
        }
    }
}
