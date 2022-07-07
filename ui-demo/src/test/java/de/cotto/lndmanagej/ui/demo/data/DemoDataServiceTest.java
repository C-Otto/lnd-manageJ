package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.C_OTTO;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.KRAKEN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DemoDataServiceTest {

    @InjectMocks
    private DemoDataService demoDataService;

    @SuppressWarnings("unused")
    @Mock
    private DemoStatusService demoStatusService;

    @SuppressWarnings("unused")
    @Mock
    private DemoWarningService warningService;

    @Test
    void getOpenChannels_deliversTestData() {
        assertNotNull(demoDataService.getOpenChannels());
        assertTrue(demoDataService.getOpenChannels().size() > 5);
    }

    @Test
    void getNode_cOtto_exists() {
        assertNotNull(demoDataService.getNode(C_OTTO.remotePubkey()));
    }

    @Test
    void getNodeDetails_cOtto_exists() {
        assertNotNull(demoDataService.getNodeDetails(C_OTTO.remotePubkey()));
    }

    @Test
    void getNodeDetails_kraken_isNotOnline() {
        NodeDetailsDto kraken = demoDataService.getNodeDetails(KRAKEN.remotePubkey());
        assertNotNull(kraken);
        assertFalse(kraken.onlineReport().online());
    }

    @Test
    void getChannelDetails_exists() throws Exception {
        demoDataService.getChannelDetails(C_OTTO.channelId());
    }

    @Test
    void getChannelDetails_unknownChannelId_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> demoDataService.getChannelDetails(CHANNEL_ID));
    }

    @Test
    void isOnline_cOtto_true() {
        assertTrue(DemoDataService.isOnline(C_OTTO.channelId()));
    }

    @Test
    void isOnline_kraken_false() {
        assertFalse(DemoDataService.isOnline(KRAKEN.channelId()));
    }

}