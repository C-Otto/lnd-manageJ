package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        verify(repository).saveAll(argThat(iterable -> iterable instanceof List && ((List<?>) iterable).size() == 2));
    }
}