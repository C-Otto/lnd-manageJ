package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL_NOT_CONNECTED;
import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL_NOT_SYNCED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceImplTest {

    @InjectMocks
    private StatusServiceImpl statusService;

    @Mock
    private OwnNodeService ownNodeService;

    @Test
    void getStatus_notConnected() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        when(ownNodeService.getBlockHeight()).thenThrow(new NoSuchElementException());
        assertThat(statusService.getStatus()).isEqualTo(STATUS_MODEL_NOT_CONNECTED);
    }

    @Test
    void getStatus_notSynced() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        when(ownNodeService.getBlockHeight()).thenReturn(111_000);
        assertThat(statusService.getStatus()).isEqualTo(STATUS_MODEL_NOT_SYNCED);
    }

    @Test
    void getStatus_synced() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        when(ownNodeService.getBlockHeight()).thenReturn(111_111);
        assertThat(statusService.getStatus()).isEqualTo(STATUS_MODEL);
    }

}