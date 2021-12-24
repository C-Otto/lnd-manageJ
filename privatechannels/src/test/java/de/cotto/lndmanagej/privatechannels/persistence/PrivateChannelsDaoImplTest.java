package de.cotto.lndmanagej.privatechannels.persistence;

import de.cotto.lndmanagej.model.ChannelId;
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
    private PrivateChannelsRepository repository;

    @Test
    void isPrivate_unknown() {
        when(repository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.empty());
        assertThat(dao.isPrivate(CHANNEL_ID)).isEmpty();
    }

    @Test
    void isPrivate_true() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, true);
        when(repository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.of(dto));
        assertThat(dao.isPrivate(CHANNEL_ID)).contains(true);
    }

    @Test
    void isPrivate_false() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, false);
        when(repository.findById(CHANNEL_ID.getShortChannelId())).thenReturn(Optional.of(dto));
        assertThat(dao.isPrivate(CHANNEL_ID)).contains(false);
    }

    @Test
    void savePrivateFlag_private() {
        dao.savePrivateFlag(CHANNEL_ID, true);
        verifySave(CHANNEL_ID, true);
    }

    @Test
    void savePrivateFlag_not_private() {
        dao.savePrivateFlag(CHANNEL_ID_2, false);
        verifySave(CHANNEL_ID_2, false);
    }

    private void verifySave(ChannelId channelId, boolean expected) {
        verify(repository).save(argThat(privateChannelJpaDto -> privateChannelJpaDto.isPrivate() == expected));
        verify(repository).save(argThat(dto -> channelId.equals(ChannelId.fromShortChannelId(dto.getChannelId()))));
    }
}