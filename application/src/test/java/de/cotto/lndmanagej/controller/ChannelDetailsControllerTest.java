package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
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
    private NodeService nodeService;

    @Mock
    private Metrics metrics;

    @Test
    void getDetails_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> channelDetailsController.getDetails(CHANNEL_ID));
    }

    @Test
    void getDetails() throws NotFoundException {
        ChannelDetailsDto expectedDetails = new ChannelDetailsDto(CHANNEL_ID, PUBKEY_2, ALIAS_2);
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));

        assertThat(channelDetailsController.getDetails(CHANNEL_ID)).isEqualTo(expectedDetails);
        verify(metrics).mark(argThat(name -> name.endsWith(".getDetails")));
    }
}