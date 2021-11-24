package de.cotto.lndmanagej.grpc;

import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.LndConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class GrpcBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final StubCreator stubCreator;
    private final Metrics metrics;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected GrpcBase(LndConfiguration lndConfiguration, Metrics metrics) throws IOException {
        stubCreator = getStubCreator(lndConfiguration);
        this.metrics = metrics;
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

    <X> Optional<X> get(String name, Supplier<X> supplier) {
        try {
            return Optional.ofNullable(metrics.timer(name).timeSupplier(supplier));
        } catch (StatusRuntimeException exception) {
            logger.warn("Exception while connecting to lnd: ", exception);
            return Optional.empty();
        }
    }

    protected void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
