package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {
    @InjectMocks
    private StatusController statusController;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private Metrics metrics;

    @Test
    void isSyncedToChain() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);

        assertThat(statusController.isSyncedToChain()).isTrue();
        verify(metrics).mark(argThat(name -> name.endsWith(".isSyncedToChain")));
    }

    @Test
    void isSyncedToChain_false() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        assertThat(statusController.isSyncedToChain()).isFalse();
    }
}