package de.cotto.lndmanagej.forwardinghistory.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_OLD;
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
        assertThat(repository.findByChannelIncomingAndTimestampGreaterThan(
                FORWARDING_EVENT.channelIn().getShortChannelId(),
                0)
        ).map(ForwardingEventJpaDto::toModel).containsExactly(FORWARDING_EVENT);
    }

    @Test
    void findByChannelIncoming_uses_max_age() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_OLD));
        assertThat(repository.findByChannelIncomingAndTimestampGreaterThan(
                FORWARDING_EVENT_OLD.channelIn().getShortChannelId(),
                FORWARDING_EVENT_OLD.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli()
        )).map(ForwardingEventJpaDto::toModel).isEmpty();
    }

    @Test
    void findByChannelOutgoing() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_2));
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT));
        assertThat(repository.findByChannelOutgoingAndTimestampGreaterThan(
                FORWARDING_EVENT.channelOut().getShortChannelId(),
                0)
        ).map(ForwardingEventJpaDto::toModel).containsExactly(FORWARDING_EVENT);
    }

    @Test
    void findByChannelOutgoing_uses_max_age() {
        repository.save(ForwardingEventJpaDto.createFromModel(FORWARDING_EVENT_OLD));
        assertThat(repository.findByChannelOutgoingAndTimestampGreaterThan(
                FORWARDING_EVENT_OLD.channelOut().getShortChannelId(),
                FORWARDING_EVENT_OLD.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli()
        )).map(ForwardingEventJpaDto::toModel).isEmpty();
    }
}