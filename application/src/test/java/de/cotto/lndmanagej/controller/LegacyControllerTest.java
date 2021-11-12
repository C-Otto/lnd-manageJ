package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class LegacyControllerTest {
    @InjectMocks
    private LegacyController legacyController;

    @Mock
    private NodeService nodeService;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS);
        assertThat(legacyController.getAlias(PUBKEY)).isEqualTo(ALIAS);
    }

    @Test
    void getOpenChannelIds() {
        when(nodeService.getOpenChannelIds(PUBKEY)).thenReturn(List.of(CHANNEL_ID, CHANNEL_ID_3));
        assertThat(legacyController.getOpenChannelIds(PUBKEY)).isEqualTo(
                CHANNEL_ID + "\n" + CHANNEL_ID_3
        );
    }
}