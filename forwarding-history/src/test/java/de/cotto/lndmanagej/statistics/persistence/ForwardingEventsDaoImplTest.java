package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ForwardingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForwardingEventsDaoImplTest {
    @InjectMocks
    private ForwardingEventsDaoImpl dao;

    @Mock
    private ForwardingEventsRepository repository;

    @Test
    void getOffset_initially_0() {
        when(repository.findMaxIndex()).thenReturn(Optional.empty());
        assertThat(dao.getOffset()).isEqualTo(0);
    }

    @Test
    void getOffset() {
        int expectedOffset = 123;
        when(repository.findMaxIndex()).thenReturn(Optional.of(expectedOffset));
        assertThat(dao.getOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void save_empty() {
        dao.save(Set.of());
        verify(repository).saveAll(List.of());
    }

    @Test
    void save_two() {
        dao.save(Set.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
        Set<ForwardingEvent> expected = Set.of(FORWARDING_EVENT, FORWARDING_EVENT_2);
        verify(repository).saveAll(argThat(isSet(expected)));
    }

    @Test
    void getEventsWithOutgoingChannel_empty() {
        assertThat(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getEventsWithOutgoingChannel() {
        when(repository.findByChannelOutgoing(CHANNEL_ID_2.getShortChannelId())).thenReturn(List.of(
                ForwardingEventJpaDto.createFromForwardingEvent(FORWARDING_EVENT),
                ForwardingEventJpaDto.createFromForwardingEvent(FORWARDING_EVENT_2)
        ));
        assertThat(dao.getEventsWithOutgoingChannel(CHANNEL_ID_2))
                .containsExactly(FORWARDING_EVENT, FORWARDING_EVENT_2);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private <S extends ForwardingEventJpaDto> ArgumentMatcher<Iterable<S>> isSet(Set<ForwardingEvent> expected) {
        return iterable -> iterable instanceof List && ((List<S>) iterable).stream()
                .map(ForwardingEventJpaDto::toModel)
                .collect(Collectors.toSet())
                .equals(expected);
    }
}