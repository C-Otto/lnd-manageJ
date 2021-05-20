package de.cotto.lndmanagej.grpc;

import lnrpc.GetInfoResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GrpcGetInfo {
    private final GrpcService grpcService;
    private GetInfoResponse info;

    public GrpcGetInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
        refreshInfo();
    }

    @Scheduled(fixedDelay = 10_000)
    private void refreshInfo() {
        info = grpcService.getInfo();
    }

    public String getPubkey() {
        return info.getIdentityPubkey();
    }

    public String getAlias() {
        return info.getAlias();
    }

    public int getBlockHeight() {
        return info.getBlockHeight();
    }
}
