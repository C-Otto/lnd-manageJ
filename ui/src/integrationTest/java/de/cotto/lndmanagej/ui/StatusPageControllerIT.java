package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.ui.controller.StatusPageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL_NOT_CONNECTED;
import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL_NOT_SYNCED;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatusPageController.class)
@Import(ChannelIdParser.class)
class StatusPageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatusService statusService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void statusPage_notConnected() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL_NOT_CONNECTED);
        mockMvc.perform(getStatusPage())
                .andExpect(status().isOk());
    }

    @Test
    void statusPage_notSynced() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL_NOT_SYNCED);
        mockMvc.perform(getStatusPage())
                .andExpect(status().isOk());
    }

    @Test
    void statusPage_synced() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        mockMvc.perform(getStatusPage())
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder getStatusPage() {
        return get("/status").servletPath("/status");
    }

}
