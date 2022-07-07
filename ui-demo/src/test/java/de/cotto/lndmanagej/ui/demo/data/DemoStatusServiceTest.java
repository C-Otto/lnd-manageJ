package de.cotto.lndmanagej.ui.demo.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DemoStatusServiceTest {

    @InjectMocks
    DemoStatusService statusService;

    @Test
    void getStatus_isSynced() {
        assertNotNull(statusService.getStatus());
        assertTrue(statusService.getStatus().synced());
    }
}