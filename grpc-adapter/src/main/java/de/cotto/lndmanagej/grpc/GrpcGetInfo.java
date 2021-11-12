package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.GetInfoResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
public class GrpcGetInfo {
    private final GrpcService grpcService;

    @Nullable
    private Pubkey pubkey;

    public GrpcGetInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Pubkey getPubkey() {
        if (pubkey == null) {
            pubkey = grpcService.getInfo().map(GetInfoResponse::getIdentityPubkey).map(Pubkey::create).orElseThrow();
        }
        return Objects.requireNonNull(pubkey);
    }

    public Optional<String> getAlias() {
        return grpcService.getInfo().map(GetInfoResponse::getAlias);
    }

    public Optional<Integer> getBlockHeight() {
        return grpcService.getInfo().map(GetInfoResponse::getBlockHeight);
    }

    public Optional<String> getBlockHash() {
        return grpcService.getInfo().map(GetInfoResponse::getBlockHash);
    }

    public Optional<Instant> getBestHeaderTimestamp() {
        return grpcService.getInfo().map(GetInfoResponse::getBestHeaderTimestamp).map(Instant::ofEpochSecond);
    }

    public Optional<String> getVersion() {
        return grpcService.getInfo().map(GetInfoResponse::getVersion);
    }

    public Optional<String> getCommitHash() {
        return grpcService.getInfo().map(GetInfoResponse::getCommitHash);
    }

    public Optional<Integer> getNumberOfActiveChannels() {
        return grpcService.getInfo().map(GetInfoResponse::getNumActiveChannels);
    }

    public Optional<Integer> getNumberOfInactiveChannels() {
        return grpcService.getInfo().map(GetInfoResponse::getNumInactiveChannels);
    }

    public Optional<Integer> getNumberOfPendingChannels() {
        return grpcService.getInfo().map(GetInfoResponse::getNumPendingChannels);
    }

    public Optional<Integer> getNumberOfPeers() {
        return grpcService.getInfo().map(GetInfoResponse::getNumPeers);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isSyncedToChain() {
        return grpcService.getInfo().map(GetInfoResponse::getSyncedToChain);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isSyncedToGraph() {
        return grpcService.getInfo().map(GetInfoResponse::getSyncedToGraph);
    }

}
