package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2_PEER;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3_PEER;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePeersUpdaterTest {
    @InjectMocks
    private OnlinePeersUpdater onlinePeersUpdater;

    @Mock
    private ChannelService channelService;

    @Mock
    private NodeService nodeService;

    @Mock
    private OnlinePeersDao dao;

    @BeforeEach
    void setUp() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
    }

    @Test
    void storePeerOnlineStatus_no_channels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of());
        onlinePeersUpdater.storePeerOnlineStatus();
    }

    @Test
    void storePeerOnlineStatus() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2);
        when(nodeService.getNode(PUBKEY_3)).thenReturn(NODE_3_PEER);

        onlinePeersUpdater.storePeerOnlineStatus();

        verify(dao).saveOnlineStatus(eq(PUBKEY_2), eq(false), any());
        verify(dao).saveOnlineStatus(eq(PUBKEY_3), eq(true), any());
    }

    @Test
    void saveOnlineStatus_online_to_offline() {
        when(dao.getMostRecentOnlineStatus(PUBKEY_2)).thenReturn(Optional.of(ONLINE_STATUS));
        when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2);

        onlinePeersUpdater.storePeerOnlineStatus();

        verify(dao).saveOnlineStatus(eq(PUBKEY_2), eq(false), any());
    }

    @Test
    void saveOnlineStatus_offline_to_online() {
        when(dao.getMostRecentOnlineStatus(PUBKEY_2)).thenReturn(Optional.of(ONLINE_STATUS_OFFLINE));
        when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2_PEER);

        onlinePeersUpdater.storePeerOnlineStatus();

        verify(dao).saveOnlineStatus(eq(PUBKEY_2), eq(true), any());
    }

    @Test
    void saveOnlineStatus_still_offline() {
        when(dao.getMostRecentOnlineStatus(PUBKEY_2)).thenReturn(Optional.of(ONLINE_STATUS_OFFLINE));
        when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2);

        onlinePeersUpdater.storePeerOnlineStatus();

        verify(dao, never()).saveOnlineStatus(any(), anyBoolean(), any());
    }

    @Test
    void saveOnlineStatus_still_online() {
        when(dao.getMostRecentOnlineStatus(PUBKEY_2)).thenReturn(Optional.of(ONLINE_STATUS));
        when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2_PEER);

        onlinePeersUpdater.storePeerOnlineStatus();

        verify(dao, never()).saveOnlineStatus(any(), anyBoolean(), any());
    }
}