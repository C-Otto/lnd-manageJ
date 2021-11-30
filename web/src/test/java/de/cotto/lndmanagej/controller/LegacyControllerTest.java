package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_4;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyControllerTest {
    @InjectMocks
    private LegacyController legacyController;

    @Mock
    private NodeService nodeService;

    @Mock
    private ChannelService channelService;

    @Test
    void getOpenChannelIdsPretty() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(nodeService.getAlias(PUBKEY_3)).thenReturn(ALIAS_3);
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        assertThat(legacyController.getOpenChannelIdsPretty()).isEqualTo(
                CHANNEL_ID_COMPACT + "\t" + PUBKEY_2 + "\t" + CAPACITY + "\t" + ALIAS_2 + "\n" +
                        CHANNEL_ID_COMPACT_4 + "\t" + PUBKEY_3 + "\t" + CAPACITY_2 + "\t" + ALIAS_3
        );
    }

    @Test
    void getOpenChannelIdsPretty_ordered() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getOpenChannelIdsPretty())
                .matches(CHANNEL_ID_COMPACT + ".*\n" + CHANNEL_ID_COMPACT_4 + ".*");
    }
}