package de.cotto.lndmanagej.forwardinghistory.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ForwardingEventsRepositoryIT {
    @Autowired
    private ForwardingEventsRepository repository;

    @Test
    void findMaxIndex_not_found() {
        assertThat(repository.findMaxIndex()).isEmpty();
    }

    @Test
    void findMaxIndex_one_event() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT));
        assertThat(repository.findMaxIndex()).contains(1);
    }

    @Test
    void findMaxIndex_two_events() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_2));
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT));
        assertThat(repository.findMaxIndex()).contains(2);
    }

    @Test
    void findByChannelIncoming() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_2));
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT));
        assertThat(repository.findByChannelIncoming(FORWARDING_EVENT.channelIn().getShortChannelId()))
                .map(ForwardingEventJpaDto::toModel)
                .containsExactly(FORWARDING_EVENT);
    }

    @Test
    void findByChannelOutgoing() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_2));
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT));
        assertThat(repository.findByChannelOutgoing(FORWARDING_EVENT.channelOut().getShortChannelId()))
                .map(ForwardingEventJpaDto::toModel)
                .containsExactly(FORWARDING_EVENT);
    }
}