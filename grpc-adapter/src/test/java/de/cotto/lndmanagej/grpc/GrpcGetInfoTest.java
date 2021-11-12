package de.cotto.lndmanagej.grpc;

import lnrpc.GetInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrpcGetInfoTest {
    private static final String ALIAS = "alias";
    private static final String VERSION = "version";
    private static final String COMMIT_HASH = "commit";
    private static final String BLOCK_HASH = "block";
    private static final int NUMBER_OF_PEERS = 100;
    private static final int NUMBER_OF_ACTIVE_CHANNELS = 200;
    private static final int NUMBER_OF_INACTIVE_CHANNELS = 300;
    private static final int NUMBER_OF_PENDING_CHANNELS = 400;
    private static final int BEST_HEADER_TIMESTAMP = 1_636_053_531;
    private static final Instant BEST_HEADER_INSTANT = Instant.ofEpochSecond(BEST_HEADER_TIMESTAMP);
    private static final int BLOCK_HEIGHT = 123;
    private GrpcGetInfo grpcGetInfo;
    private GrpcService grpcService;

    @BeforeEach
    void setUp() {
        grpcService = mock(GrpcService.class);
        GetInfoResponse response = createResponse(false, true);
        when(grpcService.getInfo()).thenReturn(Optional.of(response));
        grpcGetInfo = new GrpcGetInfo(grpcService);
    }

    private GetInfoResponse createResponse(boolean syncedToChain, boolean syncedToGraph) {
        return GetInfoResponse.newBuilder()
                .setIdentityPubkey(PUBKEY.toString())
                .setAlias(ALIAS)
                .setNumActiveChannels(NUMBER_OF_ACTIVE_CHANNELS)
                .setNumInactiveChannels(NUMBER_OF_INACTIVE_CHANNELS)
                .setNumPeers(NUMBER_OF_PEERS)
                .setNumPendingChannels(NUMBER_OF_PENDING_CHANNELS)
                .setBestHeaderTimestamp(BEST_HEADER_TIMESTAMP)
                .setCommitHash(COMMIT_HASH)
                .setVersion(VERSION)
                .setBlockHash(BLOCK_HASH)
                .setBlockHeight(BLOCK_HEIGHT)
                .setSyncedToChain(syncedToChain)
                .setSyncedToGraph(syncedToGraph)
                .build();
    }

    @Test
    void getPubkey() {
        assertThat(grpcGetInfo.getPubkey()).contains(PUBKEY);
    }

    @Test
    void getAlias() {
        assertThat(grpcGetInfo.getAlias()).contains(ALIAS);
    }

    @Test
    void getBlockHeight() {
        assertThat(grpcGetInfo.getBlockHeight()).contains(BLOCK_HEIGHT);
    }

    @Test
    void getBlockHash() {
        assertThat(grpcGetInfo.getBlockHash()).contains(BLOCK_HASH);
    }

    @Test
    void getNumberOfPeers() {
        assertThat(grpcGetInfo.getNumberOfPeers()).contains(NUMBER_OF_PEERS);
    }

    @Test
    void getNumberOfActiveChannels() {
        assertThat(grpcGetInfo.getNumberOfActiveChannels()).contains(NUMBER_OF_ACTIVE_CHANNELS);
    }

    @Test
    void getNumberOfInactiveChannels() {
        assertThat(grpcGetInfo.getNumberOfInactiveChannels()).contains(NUMBER_OF_INACTIVE_CHANNELS);
    }

    @Test
    void getNumberOfPendingChannels() {
        assertThat(grpcGetInfo.getNumberOfPendingChannels()).contains(NUMBER_OF_PENDING_CHANNELS);
    }

    @Test
    void getVersion() {
        assertThat(grpcGetInfo.getVersion()).contains(VERSION);
    }

    @Test
    void getCommitHash() {
        assertThat(grpcGetInfo.getCommitHash()).contains(COMMIT_HASH);
    }

    @Test
    void getBestHeaderTimestamp() {
        assertThat(grpcGetInfo.getBestHeaderTimestamp()).contains(BEST_HEADER_INSTANT);
    }

    @Test
    void isSyncedToChain_true() {
        when(grpcService.getInfo()).thenReturn(Optional.of(createResponse(true, true)));
        assertThat(grpcGetInfo.isSyncedToChain()).contains(true);
    }

    @Test
    void isSyncedToChain_false() {
        when(grpcService.getInfo()).thenReturn(Optional.of(createResponse(false, true)));
        assertThat(grpcGetInfo.isSyncedToChain()).contains(false);
    }

    @Test
    void isSyncedToGraph_true() {
        when(grpcService.getInfo()).thenReturn(Optional.of(createResponse(true, true)));
        assertThat(grpcGetInfo.isSyncedToGraph()).contains(true);
    }

    @Test
    void isSyncedToGraph_false() {
        when(grpcService.getInfo()).thenReturn(Optional.of(createResponse(true, false)));
        assertThat(grpcGetInfo.isSyncedToGraph()).contains(false);
    }

    @Test
    void failure() {
        when(grpcService.getInfo()).thenReturn(Optional.empty());
        grpcGetInfo = new GrpcGetInfo(grpcService);
        assertThat(grpcGetInfo.getBlockHeight()).isEmpty();
    }
}