package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.springframework.stereotype.Component;

@Component
public class OwnNodeService {
    private final GrpcGetInfo grpcGetInfo;

    public OwnNodeService(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
    }

    public boolean isSyncedToChain() {
        return grpcGetInfo.isSyncedToChain().orElse(false);
    }
}
