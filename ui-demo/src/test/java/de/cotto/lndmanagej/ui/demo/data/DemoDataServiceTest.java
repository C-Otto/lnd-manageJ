package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.controller.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.C_OTTO;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DemoDataServiceTest {

    @InjectMocks
    private DemoDataService demoDataService;

    @Test
    void getOpenChannels_deliversTestData() {
        assertNotNull(demoDataService.getOpenChannels());
        assertTrue(demoDataService.getOpenChannels().size() > 5);
    }

    @Test
    void getNode_cOttoExists() {
        assertNotNull(demoDataService.getNode(C_OTTO.remotePubkey()));
    }

    @Test
    void getNodeDetails_cOttoExists() {
        assertNotNull(demoDataService.getNodeDetails(C_OTTO.remotePubkey()));
    }

    @Test
    void getStatus_exists() {
        assertNotNull(demoDataService.getStatus());
        assertNotNull(demoDataService.getStatus().warnings());
    }

    @Test
    void getStatus_hasNodeWithWarnings() {
        assertNotNull(demoDataService.getStatus().warnings().nodesWithWarnings());
        assertFalse(demoDataService.getStatus().warnings().nodesWithWarnings().isEmpty());
    }

    @Test
    void getStatus_hasChannelWarnings() {
        assertNotNull(demoDataService.getStatus().warnings().channelsWithWarnings());
        assertFalse(demoDataService.getStatus().warnings().channelsWithWarnings().isEmpty());
    }

    @Test
    void getChannelDetails_exists() throws Exception {
        demoDataService.getChannelDetails(C_OTTO.channelId());
    }

    @Test
    void getChannelDetails_unknownChannelId_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> demoDataService.getChannelDetails(CHANNEL_ID));
    }

}