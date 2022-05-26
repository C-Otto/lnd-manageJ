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
class GrpcInvoicesServiceTest {
    private final StubCreator stubCreator = mock(StubCreator.class);

    @Mock
    private ConfigurationService configurationService;

    private GrpcInvoicesService grpcInvoicesService;

    @BeforeEach
    void setUp() throws IOException {
        grpcInvoicesService = new TestableGrpcInvoicesService(configurationService, "/home/foo");
    }

    @Test
    void shutdown() {
        grpcInvoicesService.shutdown();
        verify(stubCreator).shutdown();
    }

    public class TestableGrpcInvoicesService extends GrpcInvoicesService {
        public TestableGrpcInvoicesService(
                ConfigurationService configurationService,
                String homeDirectory
        ) throws IOException {
            super(configurationService, homeDirectory);
        }

        @Override
        protected StubCreator getStubCreator() {
            return GrpcInvoicesServiceTest.this.stubCreator;
        }
    }
}
