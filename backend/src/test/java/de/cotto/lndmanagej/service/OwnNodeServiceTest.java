package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnNodeServiceTest {
    @InjectMocks
    private OwnNodeService ownNodeService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Test
    void isSyncedToChain() {
        when(grpcGetInfo.isSyncedToChain()).thenReturn(Optional.of(true));
        assertThat(ownNodeService.isSyncedToChain()).isTrue();
    }

    @Test
    void isSyncedToChain_false() {
        when(grpcGetInfo.isSyncedToChain()).thenReturn(Optional.of(false));
        assertThat(ownNodeService.isSyncedToChain()).isFalse();
    }

    @Test
    void isSyncedToChain_empty() {
        when(grpcGetInfo.isSyncedToChain()).thenReturn(Optional.empty());
        assertThat(ownNodeService.isSyncedToChain()).isFalse();
    }

    @Test
    void getBlockHeight() {
        when(grpcGetInfo.getBlockHeight()).thenReturn(Optional.of(123_456));
        assertThat(ownNodeService.getBlockHeight()).isEqualTo(123_456);
    }

    @Test
    void getBlockHeight_empty() {
        when(grpcGetInfo.getBlockHeight()).thenReturn(Optional.empty());
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(ownNodeService::getBlockHeight);
    }
}