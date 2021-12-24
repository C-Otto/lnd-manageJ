package de.cotto.lndmanagej.privatechannels;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateChannelsResolverImplTest {
    @InjectMocks
    private PrivateChannelsResolverImpl privateChannelsResolverImpl;

    @Mock
    private PrivateChannelsDao privateChannelsDao;

    @Test
    void unknown() {
        when(privateChannelsDao.isPrivate(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(privateChannelsResolverImpl.isPrivate(CHANNEL_ID)).isFalse();
    }

    @Test
    void known_true() {
        when(privateChannelsDao.isPrivate(CHANNEL_ID)).thenReturn(Optional.of(true));
        assertThat(privateChannelsResolverImpl.isPrivate(CHANNEL_ID)).isTrue();
    }

    @Test
    void known_false() {
        when(privateChannelsDao.isPrivate(CHANNEL_ID)).thenReturn(Optional.of(false));
        assertThat(privateChannelsResolverImpl.isPrivate(CHANNEL_ID)).isFalse();
    }
}