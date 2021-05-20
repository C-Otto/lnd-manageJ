package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.LndConfiguration;
import lnrpc.GetInfoResponse;
import lnrpc.LightningGrpc;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class GrpcService {

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

    GetInfoResponse getInfo() {
        return lightningStub.getInfo(lnrpc.GetInfoRequest.getDefaultInstance());
    }
}
