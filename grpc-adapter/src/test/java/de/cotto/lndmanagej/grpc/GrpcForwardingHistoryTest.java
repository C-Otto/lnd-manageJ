package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ForwardingEvent;
import lnrpc.ForwardingHistoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcForwardingHistoryTest {
    private static final int OFFSET = 123;
    private static final int LIMIT = 1_000;

    @InjectMocks
    private GrpcForwardingHistory grpcForwardingHistory;

    @Mock
    private GrpcService grpcService;

    @Test
    void empty_optional() {
        when(grpcService.getForwardingHistory(anyInt(), anyInt())).thenReturn(Optional.empty());
        assertThat(grpcForwardingHistory.getForwardingEventsAfter(0)).isEmpty();
    }

    @Test
    void no_event() {
        ForwardingHistoryResponse response = ForwardingHistoryResponse.newBuilder().build();
        when(grpcService.getForwardingHistory(anyInt(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcForwardingHistory.getForwardingEventsAfter(0)).contains(List.of());
    }

    @Test
    void with_events() {
        ForwardingHistoryResponse response = ForwardingHistoryResponse.newBuilder()
                .setLastOffsetIndex(FORWARDING_EVENT_2.index())
                .addForwardingEvents(event(FORWARDING_EVENT))
                .addForwardingEvents(event(FORWARDING_EVENT_2))
                .build();
        when(grpcService.getForwardingHistory(anyInt(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcForwardingHistory.getForwardingEventsAfter(0)).contains(
                List.of(FORWARDING_EVENT, FORWARDING_EVENT_2)
        );
    }

    @Test
    void starts_at_the_beginning() {
        grpcForwardingHistory.getForwardingEventsAfter(OFFSET);
        verify(grpcService).getForwardingHistory(eq(OFFSET), anyInt());
    }

    @Test
    void uses_limit() {
        grpcForwardingHistory.getForwardingEventsAfter(OFFSET);
        verify(grpcService).getForwardingHistory(OFFSET, LIMIT);
    }

    @Test
    void getLimit() {
        assertThat(grpcForwardingHistory.getLimit()).isEqualTo(LIMIT);
    }

    private lnrpc.ForwardingEvent event(ForwardingEvent forwardingEvent) {
        return lnrpc.ForwardingEvent.newBuilder()
                .setAmtInMsat(forwardingEvent.amountIn().milliSatoshis())
                .setAmtOutMsat(forwardingEvent.amountOut().milliSatoshis())
                .setChanIdIn(forwardingEvent.channelIn().getShortChannelId())
                .setChanIdOut(forwardingEvent.channelOut().getShortChannelId())
                .setTimestampNs(forwardingEvent.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli() * 1_000_000)
                .build();
    }
}