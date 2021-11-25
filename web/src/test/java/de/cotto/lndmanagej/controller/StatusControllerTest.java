package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.PubkeysDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {
    @InjectMocks
    private StatusController statusController;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private Metrics metrics;

    @Mock
    private ChannelService channelService;

    @Test
    void isSyncedToChain() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);

        assertThat(statusController.isSyncedToChain()).isTrue();
        verify(metrics).mark(argThat(name -> name.endsWith(".isSyncedToChain")));
    }

    @Test
    void isSyncedToChain_false() {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        assertThat(statusController.isSyncedToChain()).isFalse();
    }

    @Test
    void getPubkeysForOpenChannels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<Pubkey> expectedPubkeys =
                List.of(LOCAL_OPEN_CHANNEL.getRemotePubkey(), LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey());
        assertThat(statusController.getPubkeysForOpenChannels()).isEqualTo(new PubkeysDto(expectedPubkeys));
        verify(metrics).mark(argThat(name -> name.endsWith(".getPubkeysForOpenChannels")));
    }

    @Test
    void getPeerPubkeys_without_no_duplicates() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        assertThat(statusController.getPubkeysForOpenChannels().pubkeys()).containsExactly(PUBKEY_2.toString());
    }
}