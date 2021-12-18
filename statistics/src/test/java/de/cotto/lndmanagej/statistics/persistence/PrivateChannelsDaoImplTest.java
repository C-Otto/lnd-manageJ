package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateChannelsDaoImplTest {

    @InjectMocks
    private PrivateChannelsDaoImpl dao;

    @Mock
    private PrivateChannelsRepository privateChannelsRepository;

    @Test
    void isPrivate_unknown() {
        when(privateChannelsRepository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.empty());
        assertThat(dao.isPrivate(CHANNEL_ID)).isEmpty();
    }

    @Test
    void isPrivate_true() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, true);
        when(privateChannelsRepository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.of(dto));
        assertThat(dao.isPrivate(CHANNEL_ID)).contains(true);
    }

    @Test
    void isPrivate_false() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, false);
        when(privateChannelsRepository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.of(dto));
        assertThat(dao.isPrivate(CHANNEL_ID)).contains(false);
    }

    @Test
    void savePrivateFlag_private() {
        dao.savePrivateFlag(CHANNEL_ID, true);
        verify(privateChannelsRepository).save(argThat(PrivateChannelJpaDto::isPrivate));
        verify(privateChannelsRepository).save(argThat(
                privateChannelJpaDto -> CHANNEL_ID.getShortChannelId() == privateChannelJpaDto.getChannelId()
        ));
    }

    @Test
    void savePrivateFlag_not_private() {
        dao.savePrivateFlag(CHANNEL_ID_2, false);
        verify(privateChannelsRepository).save(argThat(
                privateChannelJpaDto -> !privateChannelJpaDto.isPrivate()
        ));
        verify(privateChannelsRepository).save(argThat(
                privateChannelJpaDto -> CHANNEL_ID_2.getShortChannelId() == privateChannelJpaDto.getChannelId()
        ));
    }
}