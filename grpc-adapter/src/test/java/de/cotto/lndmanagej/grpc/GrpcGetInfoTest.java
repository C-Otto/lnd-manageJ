package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Node;
import lnrpc.GetInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrpcGetInfoTest {

    private static final String PUBKEY = "pubkey";
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
        GetInfoResponse response1 = createResponse(BLOCK_HEIGHT, false, true);
        GetInfoResponse response2 = createResponse(BLOCK_HEIGHT + 1, true, false);
        when(grpcService.getInfo()).thenReturn(Optional.of(response1)).thenReturn(Optional.of(response2));
        grpcGetInfo = new GrpcGetInfo(grpcService);
    }

    private GetInfoResponse createResponse(int blockHeight, boolean syncedToChain, boolean syncedToGraph) {
        return GetInfoResponse.newBuilder()
                .setIdentityPubkey(PUBKEY)
                .setAlias(ALIAS)
                .setNumActiveChannels(NUMBER_OF_ACTIVE_CHANNELS)
                .setNumInactiveChannels(NUMBER_OF_INACTIVE_CHANNELS)
                .setNumPeers(NUMBER_OF_PEERS)
                .setNumPendingChannels(NUMBER_OF_PENDING_CHANNELS)
                .setBestHeaderTimestamp(BEST_HEADER_TIMESTAMP)
                .setCommitHash(COMMIT_HASH)
                .setVersion(VERSION)
                .setBlockHash(BLOCK_HASH)
                .setBlockHeight(blockHeight)
                .setSyncedToChain(syncedToChain)
                .setSyncedToGraph(syncedToGraph)
                .build();
    }

    @Test
    void getNode() {
        assertThat(grpcGetInfo.getNode()).isEqualTo(Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).build());
    }

    @Test
    void getPubkey() {
        assertThat(grpcGetInfo.getPubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void getAlias() {
        assertThat(grpcGetInfo.getAlias()).isEqualTo(ALIAS);
    }

    @Test
    void getBlockHeight() {
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void getBlockHash() {
        assertThat(grpcGetInfo.getBlockHash()).isEqualTo(BLOCK_HASH);
    }

    @Test
    void getNumberOfPeers() {
        assertThat(grpcGetInfo.getNumberOfPeers()).isEqualTo(NUMBER_OF_PEERS);
    }

    @Test
    void getNumberOfActiveChannels() {
        assertThat(grpcGetInfo.getNumberOfActiveChannels()).isEqualTo(NUMBER_OF_ACTIVE_CHANNELS);
    }

    @Test
    void getNumberOfInactiveChannels() {
        assertThat(grpcGetInfo.getNumberOfInactiveChannels()).isEqualTo(NUMBER_OF_INACTIVE_CHANNELS);
    }

    @Test
    void getNumberOfPendingChannels() {
        assertThat(grpcGetInfo.getNumberOfPendingChannels()).isEqualTo(NUMBER_OF_PENDING_CHANNELS);
    }

    @Test
    void getVersion() {
        assertThat(grpcGetInfo.getVersion()).isEqualTo(VERSION);
    }

    @Test
    void getCommitHash() {
        assertThat(grpcGetInfo.getCommitHash()).isEqualTo(COMMIT_HASH);
    }

    @Test
    void getBestHeaderTimestamp() {
        assertThat(grpcGetInfo.getBestHeaderTimestamp()).isEqualTo(BEST_HEADER_INSTANT);
    }

    @Test
    void isSyncedToChain() {
        assertThat(grpcGetInfo.isSyncedToChain()).isFalse();
        grpcGetInfo.refreshInfo();
        assertThat(grpcGetInfo.isSyncedToChain()).isTrue();
    }

    @Test
    void isSyncedToGraph_true() {
        assertThat(grpcGetInfo.isSyncedToGraph()).isTrue();
        grpcGetInfo.refreshInfo();
        assertThat(grpcGetInfo.isSyncedToGraph()).isFalse();
    }

    @Test
    void caches_response() {
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void updates_response() {
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
        grpcGetInfo.refreshInfo();
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT + 1);
    }

    @Test
    void does_not_update_response_on_failure() {
        when(grpcService.getInfo()).thenReturn(Optional.empty());
        grpcGetInfo.refreshInfo();
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void refreshesAfterFailure() {
        GetInfoResponse response = createResponse(BLOCK_HEIGHT, false, true);
        when(grpcService.getInfo()).thenReturn(Optional.empty()).thenReturn(Optional.of(response));
        grpcGetInfo = new GrpcGetInfo(grpcService);
        assertThat(grpcGetInfo.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }
}