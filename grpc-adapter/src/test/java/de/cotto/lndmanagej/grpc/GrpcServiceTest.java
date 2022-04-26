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
class GrpcServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);
    private TestableGrpcService grpcService;

    @Mock
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() throws IOException {
        grpcService = new TestableGrpcService(configurationService, "/home/foo");
    }

    @Test
    void shutdown() {
        grpcService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcService extends GrpcService {
        public TestableGrpcService(ConfigurationService configurationService, String homeDirectory) throws IOException {
            super(configurationService, homeDirectory);
        }

        @Override
        protected StubCreator getStubCreator() {
            return GrpcServiceTest.this.stubCreator;
        }
    }
}
