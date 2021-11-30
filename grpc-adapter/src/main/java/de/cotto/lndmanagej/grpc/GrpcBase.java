package de.cotto.lndmanagej.grpc;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.LndConfiguration;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class GrpcBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final StubCreator stubCreator;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected GrpcBase(LndConfiguration lndConfiguration) throws IOException {
        stubCreator = getStubCreator(lndConfiguration);
    }

    @VisibleForTesting
    protected StubCreator getStubCreator(LndConfiguration lndConfiguration) throws IOException {
        return new StubCreator(
                lndConfiguration.getMacaroonFile(),
                lndConfiguration.getCertFile(),
                lndConfiguration.getPort(),
                lndConfiguration.getHost()
        );
    }

    <X> Optional<X> get(Supplier<X> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (StatusRuntimeException exception) {
            logger.warn("Exception while connecting to lnd: ", exception);
            return Optional.empty();
        }
    }
}
