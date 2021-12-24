package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.privatechannels.PrivateChannelsDao;
import de.cotto.lndmanagej.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateChannelsUpdaterTest {
    @InjectMocks
    private PrivateChannelsUpdater privateResolver;

    @Mock
    private PrivateChannelsDao privateChannelsDao;

    @Mock
    private ChannelService channelService;

    @Test
    void storePrivateFlags() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_PRIVATE, LOCAL_OPEN_CHANNEL_2));
        privateResolver.storePrivateFlags();
        verify(privateChannelsDao).savePrivateFlag(CHANNEL_ID, true);
        verify(privateChannelsDao).savePrivateFlag(CHANNEL_ID_2, false);
    }
}