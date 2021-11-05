package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Node;
import lnrpc.GetInfoResponse;
import lnrpc.GetInfoResponseOrBuilder;
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

    public Node getNode() {
        return Node.builder().withPubkey(getPubkey()).withAlias(getAlias()).build();
    }

    public String getPubkey() {
        return getInfo().getIdentityPubkey();
    }

    public String getAlias() {
        return getInfo().getAlias();
    }

    public int getBlockHeight() {
        return getInfo().getBlockHeight();
    }

    public String getBlockHash() {
        return getInfo().getBlockHash();
    }

    public Instant getBestHeaderTimestamp() {
        return Instant.ofEpochSecond(getInfo().getBestHeaderTimestamp());
    }

    public String getVersion() {
        return getInfo().getVersion();
    }

    public String getCommitHash() {
        return getInfo().getCommitHash();
    }

    public int getNumberOfActiveChannels() {
        return getInfo().getNumActiveChannels();
    }

    public int getNumberOfInactiveChannels() {
        return getInfo().getNumInactiveChannels();
    }

    public int getNumberOfPendingChannels() {
        return getInfo().getNumPendingChannels();
    }

    public int getNumberOfPeers() {
        return getInfo().getNumPeers();
    }

    public boolean isSyncedToChain() {
        return getInfo().getSyncedToChain();
    }

    public boolean isSyncedToGraph() {
        return getInfo().getSyncedToGraph();
    }

    @Scheduled(fixedDelay = 60_000)
    final void refreshInfo() {
        grpcService.getInfo().ifPresent(newInfo -> info = newInfo);
    }

    private GetInfoResponseOrBuilder getInfo() {
        if (info == null) {
            refreshInfo();
        }
        return Objects.requireNonNull(info);
    }
}
