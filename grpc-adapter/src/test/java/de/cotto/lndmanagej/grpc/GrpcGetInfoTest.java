package de.cotto.lndmanagej.grpc;

import lnrpc.GetInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrpcGetInfoTest {

    private static final String PUBKEY = "pubkey";
    private static final String ALIAS = "alias";
    private static final int BLOCK_HEIGHT = 123;
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        GrpcService grpcService = mock(GrpcService.class);
        GetInfoResponse response = GetInfoResponse.newBuilder()
                .setIdentityPubkey(PUBKEY)
                .setAlias(ALIAS)
                .setBlockHeight(BLOCK_HEIGHT)
                .build();
        when(grpcService.getInfo()).thenReturn(response);
        grpcGetInfo = new GrpcGetInfo(grpcService);
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
}