package de.cotto.lndmanagej;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InfoLogger {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GrpcGetInfo grpcGetInfo;

    public InfoLogger(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
    }

    @Scheduled(fixedRate = 60_000)
    public void logAlias() {
        logger.info("Alias: {}", grpcGetInfo.getAlias());
    }

    @Scheduled(fixedRate = 60_000)
    public void logPubkey() {
        logger.info("Pubkey: {}", grpcGetInfo.getPubkey());
    }

    @Scheduled(fixedRate = 5_000)
    public void logBlockHeight() {
        logger.info("Block Height: {}", grpcGetInfo.getBlockHeight());
    }
}
