package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelDetailsControllerTest {
    @InjectMocks
    private ChannelDetailsController channelDetailsController;

    @Mock
    private ChannelService channelService;

    @Mock
    private Metrics metrics;

    @Test
    void getChannelDetails_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelDetailsController.getChannelDetails(CHANNEL_ID));
    }

    @Test
    void getChannelDetails() throws NotFoundException {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        assertThat(channelDetailsController.getChannelDetails(CHANNEL_ID))
                .isEqualTo(new ChannelDetailsDto(CHANNEL_ID));
        verify(metrics).mark(argThat(name -> name.endsWith(".getChannelDetails")));
    }
}