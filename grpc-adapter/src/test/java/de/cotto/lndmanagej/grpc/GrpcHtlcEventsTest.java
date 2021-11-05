package de.cotto.lndmanagej.grpc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.ForwardEvent;
import routerrpc.RouterOuterClass.ForwardFailEvent;
import routerrpc.RouterOuterClass.HtlcEvent;
import routerrpc.RouterOuterClass.SettleEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ForwardFailureFixtures.FORWARD_FAILURE;
import static de.cotto.lndmanagej.model.SettledForwardFixtures.SETTLED_FORWARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcHtlcEventsTest {
    @InjectMocks
    private GrpcHtlcEvents grpcHtlcEvents;

    @Mock
    private GrpcService grpcService;

    private final ForwardEvent forwardEvent = ForwardEvent.newBuilder()
            .setInfo(RouterOuterClass.HtlcInfo.newBuilder()
                    .setIncomingTimelock(1)
                    .setOutgoingTimelock(2)
                    .setIncomingAmtMsat(100)
                    .setOutgoingAmtMsat(200)
                    .build())
            .build();

    private final HtlcEvent attemptEvent = getAttempt(CHANNEL_ID.shortChannelId(), CHANNEL_ID_2.shortChannelId());

    @Nested
    class GetForwardFailures {

        private final HtlcEvent failEvent = getFailEvent(
                CHANNEL_ID.shortChannelId(),
                CHANNEL_ID_2.shortChannelId()
        );

        @Test
        void empty() {
            when(grpcService.getHtlcEvents()).thenReturn(Collections.emptyIterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void with_other_event() {
            when(grpcService.getHtlcEvents()).thenReturn(Set.of(HtlcEvent.getDefaultInstance()).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void with_fail_event() {
            when(grpcService.getHtlcEvents()).thenReturn(Set.of(failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void with_attempt_and_fail_event() {
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).containsExactly(FORWARD_FAILURE);
        }

        @Test
        void attempt_is_deleted_when_used() {
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, failEvent, failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).hasSize(1);
        }

        @Test
        void with_other_attempt_and_fail_event() {
            HtlcEvent attemptEvent = getAttempt(CHANNEL_ID.shortChannelId(), CHANNEL_ID_3.shortChannelId());
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void ignores_failure_event_with_zero_incoming_channel_id() {
            HtlcEvent failEvent = getFailEvent(0, CHANNEL_ID_2.shortChannelId());
            when(grpcService.getHtlcEvents()).thenReturn(List.of(failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void ignores_failure_event_with_zero_outgoing_channel_id() {
            HtlcEvent failEvent = getFailEvent(CHANNEL_ID.shortChannelId(), 0);
            when(grpcService.getHtlcEvents()).thenReturn(List.of(failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void removeOldEvents() {
            long timestamp = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli() * 1_000_000;
            createAndProcessOldEvent(timestamp);
            grpcHtlcEvents.removeOldEvents();
            when(grpcService.getHtlcEvents()).thenReturn(List.of(failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        @Test
        void removeOldEvents_not_old_enough() {
            long timestamp = Instant.now().minus(23, ChronoUnit.HOURS).toEpochMilli() * 1_000_000;
            createAndProcessOldEvent(timestamp);
            grpcHtlcEvents.removeOldEvents();
            when(grpcService.getHtlcEvents()).thenReturn(List.of(failEvent).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isNotEmpty();
        }

        private void createAndProcessOldEvent(long timestamp) {
            HtlcEvent oldAttempt = getBuilderWithDefaults(CHANNEL_ID.shortChannelId(), CHANNEL_ID_2.shortChannelId())
                    .setTimestampNs(timestamp)
                    .setForwardEvent(forwardEvent)
                    .build();
            when(grpcService.getHtlcEvents()).thenReturn(List.of(oldAttempt).iterator());
            assertThat(grpcHtlcEvents.getForwardFailures()).isEmpty();
        }

        private HtlcEvent getFailEvent(long incomingChannelId, long outgoingChannelId) {
            ForwardFailEvent forwardFailEvent = ForwardFailEvent.getDefaultInstance();
            return getBuilderWithDefaults(incomingChannelId, outgoingChannelId)
                    .setForwardFailEvent(forwardFailEvent)
                    .build();
        }
    }

    @Nested
    class GetSettledForwards {

        private final HtlcEvent settleEvent = getSettleEvent(
                CHANNEL_ID.shortChannelId(),
                CHANNEL_ID_2.shortChannelId()
        );

        @Test
        void empty() {
            when(grpcService.getHtlcEvents()).thenReturn(Collections.emptyIterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void with_other_event() {
            when(grpcService.getHtlcEvents()).thenReturn(Set.of(HtlcEvent.getDefaultInstance()).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void with_settle_event() {
            when(grpcService.getHtlcEvents()).thenReturn(Set.of(settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void with_attempt_and_settle_event() {
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).containsExactly(SETTLED_FORWARD);
        }

        @Test
        void attempt_is_deleted_when_used() {
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, settleEvent, settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).hasSize(1);
        }

        @Test
        void with_other_attempt_and_settle_event() {
            HtlcEvent attemptEvent = getAttempt(CHANNEL_ID.shortChannelId(), CHANNEL_ID_3.shortChannelId());
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent, settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void ignores_attempt_with_zero_incoming_channel_id() {
            HtlcEvent attemptEvent = getAttempt(0, CHANNEL_ID_2.shortChannelId());
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void ignores_attempt_with_zero_outgoing_channel_id() {
            HtlcEvent attemptEvent = getAttempt(CHANNEL_ID.shortChannelId(), 0);
            when(grpcService.getHtlcEvents()).thenReturn(List.of(attemptEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void ignores_settle_event_with_zero_incoming_channel_id() {
            HtlcEvent settleEvent = getSettleEvent(0, CHANNEL_ID_2.shortChannelId());
            when(grpcService.getHtlcEvents()).thenReturn(List.of(settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        @Test
        void ignores_settle_event_with_zero_outgoing_channel_id() {
            HtlcEvent settleEvent = getSettleEvent(CHANNEL_ID.shortChannelId(), 0);
            when(grpcService.getHtlcEvents()).thenReturn(List.of(settleEvent).iterator());
            assertThat(grpcHtlcEvents.getSettledForwards()).isEmpty();
        }

        private HtlcEvent getSettleEvent(long incomingChannelId, long outgoingChannelId) {
            SettleEvent settleEvent = SettleEvent.getDefaultInstance();
            return getBuilderWithDefaults(incomingChannelId, outgoingChannelId)
                    .setSettleEvent(settleEvent)
                    .build();
        }
    }

    private HtlcEvent getAttempt(long incomingChannelId, long outgoingChannelId) {
        return getBuilderWithDefaults(incomingChannelId, outgoingChannelId)
                .setForwardEvent(forwardEvent)
                .build();
    }

    private HtlcEvent.Builder getBuilderWithDefaults(long incomingChannelId, long outgoingChannelId) {
        return HtlcEvent.newBuilder()
                .setIncomingHtlcId(1)
                .setOutgoingHtlcId(2)
                .setTimestampNs(789)
                .setIncomingChannelId(incomingChannelId)
                .setOutgoingChannelId(outgoingChannelId);
    }
}