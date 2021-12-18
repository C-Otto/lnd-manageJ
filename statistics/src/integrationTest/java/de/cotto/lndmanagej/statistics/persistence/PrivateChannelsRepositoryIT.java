package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PrivateChannelsRepositoryIT {
    @Autowired
    private PrivateChannelsRepository repository;

    @Test
    void unknown() {
        assertThat(repository.findById(CHANNEL_ID.getShortChannelId())).isEmpty();
    }

    @Test
    void isTrue() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, true);
        repository.save(dto);
        assertThat(repository.findById(CHANNEL_ID.getShortChannelId()))
                .map(PrivateChannelJpaDto::isPrivate).contains(true);
    }

    @Test
    void isFalse() {
        PrivateChannelJpaDto dto = new PrivateChannelJpaDto(CHANNEL_ID, false);
        repository.save(dto);
        assertThat(repository.findById(CHANNEL_ID.getShortChannelId()))
                .map(PrivateChannelJpaDto::isPrivate).contains(false);
    }
}