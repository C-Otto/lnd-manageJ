package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.PendingChannelsPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingChannelsControllerTest {

    @InjectMocks
    private PendingChannelsController pendingChannelsController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void pendingChannels() {
        when(pageService.pendingChannels()).thenReturn(new PendingChannelsPage(List.of()));
        assertThat(pendingChannelsController.pendingChannelsPage(model)).isEqualTo("pending-channels-page");
        verify(model).addAllAttributes(Map.of("pendingOpenChannels", List.of()));
    }
}
