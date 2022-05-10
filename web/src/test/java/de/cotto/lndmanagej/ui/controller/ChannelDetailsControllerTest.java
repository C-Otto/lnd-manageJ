package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static de.cotto.lndmanagej.controller.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelDetailsControllerTest {
    @InjectMocks
    private ChannelDetailsController channelDetailsController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void channelDetails() throws NotFoundException {
        when(pageService.channelDetails(CHANNEL_ID)).thenReturn(new ChannelDetailsPage(CHANNEL_DETAILS_DTO));
        assertThat(channelDetailsController.channelDetails(CHANNEL_ID, model)).isEqualTo("channel-details");
    }

    @Test
    void channelDetails_not_found() throws NotFoundException {
        when(pageService.channelDetails(CHANNEL_ID)).thenThrow(NotFoundException.class);
        when(pageService.error("Channel not found.")).thenReturn(new ErrorPage("x"));
        assertThat(channelDetailsController.channelDetails(CHANNEL_ID, model)).isEqualTo("error");
    }
}
