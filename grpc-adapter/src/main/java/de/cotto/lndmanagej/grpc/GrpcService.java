package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import io.grpc.StatusRuntimeException;
import lnrpc.GetInfoResponse;
import lnrpc.LightningGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Optional;

@Component
public class GrpcService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LightningGrpc.LightningBlockingStub lightningStub;
    private final StubCreator stubCreator;

    public GrpcService(LndConfiguration lndConfiguration) throws IOException {
        stubCreator = new StubCreator(
                lndConfiguration.getMacaroonFile(),
                lndConfiguration.getCertFile(),
                lndConfiguration.getPort(),
                lndConfiguration.getHost()
        );
        lightningStub = this.stubCreator.getLightningStub();
    }

    @PreDestroy
    public void shutdown() {
        stubCreator.shutdown();
    }

    Optional<GetInfoResponse> getInfo() {
        try {
            return Optional.of(lightningStub.getInfo(lnrpc.GetInfoRequest.getDefaultInstance()));
        } catch (StatusRuntimeException exception) {
            logger.warn("Exception while connecting to lnd: ", exception);
            return Optional.empty();
        }
    }
}
