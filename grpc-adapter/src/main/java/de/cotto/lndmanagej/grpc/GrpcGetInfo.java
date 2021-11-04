package de.cotto.lndmanagej.grpc;

import lnrpc.GetInfoResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;

@Component
public class GrpcGetInfo {
    private final GrpcService grpcService;

    @Nullable
    private GetInfoResponse info;

    public GrpcGetInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
        refreshInfo();
    }

    public String getPubkey() {
        return Objects.requireNonNull(info).getIdentityPubkey();
    }

    public String getAlias() {
        return Objects.requireNonNull(info).getAlias();
    }

    public int getBlockHeight() {
        return Objects.requireNonNull(info).getBlockHeight();
    }

    public String getBlockHash() {
        return Objects.requireNonNull(info).getBlockHash();
    }

    public Instant getBestHeaderTimestamp() {
        return Instant.ofEpochSecond(Objects.requireNonNull(info).getBestHeaderTimestamp());
    }

    public String getVersion() {
        return Objects.requireNonNull(info).getVersion();
    }

    public String getCommitHash() {
        return Objects.requireNonNull(info).getCommitHash();
    }

    public int getNumberOfActiveChannels() {
        return Objects.requireNonNull(info).getNumActiveChannels();
    }

    public int getNumberOfInactiveChannels() {
        return Objects.requireNonNull(info).getNumInactiveChannels();
    }

    public int getNumberOfPendingChannels() {
        return Objects.requireNonNull(info).getNumPendingChannels();
    }

    public int getNumberOfPeers() {
        return Objects.requireNonNull(info).getNumPeers();
    }

    public boolean isSyncedToChain() {
        return Objects.requireNonNull(info).getSyncedToChain();
    }

    public boolean isSyncedToGraph() {
        return Objects.requireNonNull(info).getSyncedToGraph();
    }

    @Scheduled(fixedDelay = 60_000)
    final void refreshInfo() {
        grpcService.getInfo().ifPresent(newInfo -> info = newInfo);
    }
}
