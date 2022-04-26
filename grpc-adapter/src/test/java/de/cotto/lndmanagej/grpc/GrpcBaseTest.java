package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.Timer;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GrpcBaseTest {
    private static final String HOME_DIRECTORY = "/home/foo";

    private TestableGrpcBase grpcBase;

    @Mock
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() throws IOException {
        Timer timer = mock(Timer.class);
        lenient().when(timer.timeSupplier(any())).then(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
        grpcBase = new TestableGrpcBase(configurationService, HOME_DIRECTORY);
    }

    @Test
    void get() {
        assertThat(grpcBase.get(() -> "x")).contains("x");
    }

    @Test
    void getMacaroonFile_default() {
        assertThat(grpcBase.getMacaroonFile())
                .isEqualTo(new File(HOME_DIRECTORY + "/.lnd/data/chain/bitcoin/mainnet/admin.macaroon"));
    }

    @Test
    void getCertFile_default() {
        assertThat(grpcBase.getCertFile())
                .isEqualTo(new File(HOME_DIRECTORY + "/.lnd/tls.cert"));
    }

    @Test
    void host_default() {
        assertThat(grpcBase.getHost()).isEqualTo("localhost");
    }

    @Test
    void port_default() {
        assertThat(grpcBase.getPort()).isEqualTo(10_009);
    }

    @Test
    void get_failure() {
        assertThat(grpcBase.get(() -> {
            throw new StatusRuntimeException(Status.ABORTED);
        })).isEmpty();
    }
}
