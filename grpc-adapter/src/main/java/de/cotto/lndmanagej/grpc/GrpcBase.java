package de.cotto.lndmanagej.grpc;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.configuration.ConfigurationService;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class GrpcBase {
    @SuppressWarnings("PMD.LongVariable")
    private static final String DEFAULT_LND_MACAROON_FILE_IN_HOME_DIRECTORY =
            "/.lnd/data/chain/bitcoin/mainnet/admin.macaroon";
    @SuppressWarnings("PMD.LongVariable")
    private static final String DEFAULT_LND_CERT_FILE_IN_HOME_DIRECTORY = "/.lnd/tls.cert";
    private static final int DEFAULT_LND_PORT = 10_009;
    private static final String DEFAULT_LND_HOST = "localhost";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationService configurationService;
    private final String homeDirectory;
    protected final StubCreator stubCreator;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected GrpcBase(ConfigurationService configurationService, String homeDirectory) throws IOException {
        this.configurationService = configurationService;
        this.homeDirectory = homeDirectory;
        stubCreator = getStubCreator();
    }

    @VisibleForTesting
    protected StubCreator getStubCreator() throws IOException {
        return new StubCreator(getMacaroonFile(), getCertFile(), getPort(), getHost());
    }

    <X> Optional<X> get(Supplier<X> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (StatusRuntimeException exception) {
            logger.warn("Exception while connecting to lnd: ", exception);
            return Optional.empty();
        }
    }

    @VisibleForTesting
    File getMacaroonFile() {
        return configurationService.getLndMacaroonFile()
                .map(File::new)
                .orElse(new File(homeDirectory + DEFAULT_LND_MACAROON_FILE_IN_HOME_DIRECTORY));
    }

    @VisibleForTesting
    File getCertFile() {
        return configurationService.getLndCertFile()
                .map(File::new)
                .orElse(new File(homeDirectory + DEFAULT_LND_CERT_FILE_IN_HOME_DIRECTORY));
    }

    @VisibleForTesting
    int getPort() {
        return configurationService.getLndPort().orElse(DEFAULT_LND_PORT);
    }

    @VisibleForTesting
    String getHost() {
        return configurationService.getLndHost().orElse(DEFAULT_LND_HOST);
    }
}
