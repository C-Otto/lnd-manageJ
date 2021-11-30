package de.cotto.lndmanagej.forwardinghistory;

import de.cotto.lndmanagej.grpc.GrpcForwardingHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForwardingHistoryTest {
    private static final int OFFSET = 1_234;

    @InjectMocks
    private ForwardingHistory service;

    @Mock
    private GrpcForwardingHistory grpcForwardingHistory;

    @Mock
    private ForwardingEventsDao dao;

    @BeforeEach
    void setUp() {
        when(dao.getOffset()).thenReturn(OFFSET);
        when(grpcForwardingHistory.getLimit()).thenReturn(1);
    }

    @Test
    void refresh_uses_known_offset() {
        service.refresh();
        verify(grpcForwardingHistory).getForwardingEventsAfter(OFFSET);
    }

    @Test
    void refresh_saves_events() {
        when(grpcForwardingHistory.getForwardingEventsAfter(OFFSET)).thenReturn(
                Optional.of(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2))
        ).thenReturn(Optional.of(List.of()));
        service.refresh();
        verify(dao).save(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
    }

    @Test
    void refresh_repeats_while_at_limit() {
        when(grpcForwardingHistory.getForwardingEventsAfter(OFFSET))
                .thenReturn(Optional.of(List.of(FORWARDING_EVENT)))
                .thenReturn(Optional.of(List.of(FORWARDING_EVENT_2)))
                .thenReturn(Optional.of(List.of()));
        service.refresh();
        verify(dao).save(List.of(FORWARDING_EVENT));
        verify(dao).save(List.of(FORWARDING_EVENT_2));
    }
}