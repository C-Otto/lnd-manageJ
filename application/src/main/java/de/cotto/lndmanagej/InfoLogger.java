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

    @Scheduled(fixedRate = 10_000)
    public void logDetails() {
        logger.info("Alias: {}", grpcGetInfo.getAlias());
        logger.info("Pubkey: {}", grpcGetInfo.getPubkey());
        logger.info("Block Height: {}", grpcGetInfo.getBlockHeight());
        logger.info("Block: {}", grpcGetInfo.getBlockHash());
        logger.info("Best Header Timestamp: {}", grpcGetInfo.getBestHeaderTimestamp());
        logger.info("Active Channels: {}", grpcGetInfo.getNumberOfActiveChannels());
        logger.info("Inactive Channels: {}", grpcGetInfo.getNumberOfInactiveChannels());
        logger.info("Pending Channels: {}", grpcGetInfo.getNumberOfPendingChannels());
        logger.info("Peers: {}", grpcGetInfo.getNumberOfPeers());
        logger.info("Version: {}", grpcGetInfo.getVersion());
        logger.info("Commit: {}", grpcGetInfo.getCommitHash());
        logger.info("Synced to graph: {}", grpcGetInfo.isSyncedToGraph());
        logger.info("Synced to chain: {}", grpcGetInfo.isSyncedToChain());
    }
}
