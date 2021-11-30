package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
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
    private TestableGrpcService grpcService;

    @BeforeEach
    void setUp() throws IOException {
        grpcService = new TestableGrpcService(mock(LndConfiguration.class));
    }

    @Test
    void shutdown() {
        grpcService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcService extends GrpcService {
        public TestableGrpcService(LndConfiguration lndConfiguration) throws IOException {
            super(lndConfiguration);
        }

        @Override
        protected StubCreator getStubCreator(LndConfiguration lndConfiguration) {
            return GrpcServiceTest.this.stubCreator;
        }
    }
}