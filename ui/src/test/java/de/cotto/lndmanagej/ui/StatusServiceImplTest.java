package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceImplTest {

    @InjectMocks
    private StatusServiceImpl statusService;

    @Mock
    private OwnNodeService ownNodeService;

    @Test
    void getWarnings_synced() {
        int blockHeight = 111_111;
        when(ownNodeService.getBlockHeight()).thenReturn(blockHeight);
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        assertThat(statusService.getStatus()).isEqualTo(STATUS_MODEL);
    }

}