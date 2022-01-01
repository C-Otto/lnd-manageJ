package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForwardingEventsServiceTest {
    private static final Duration DURATION = Duration.ofDays(365 * 1_000);

    @InjectMocks
    private ForwardingEventsService forwardingEventsService;

    @Mock
    private ForwardingEventsDao dao;

    @Mock
    private ChannelService channelService;

    @Mock
    private OwnNodeService ownNodeService;

    @BeforeEach
    void setUp() {
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(BLOCK_HEIGHT);
    }

    @Test
    void getEventsWithOutgoingChannel_closed() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, DURATION))
                .thenReturn(List.of(FORWARDING_EVENT));
        assertThat(forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, DURATION))
                .isEqualTo(List.of(FORWARDING_EVENT));
    }

    @Test
    void getEventsWithIncomingChannel_closed() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID, DURATION))
                .thenReturn(List.of(FORWARDING_EVENT));
        assertThat(forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, DURATION))
                .isEqualTo(List.of(FORWARDING_EVENT));
    }

    @Test
    void getEventsWithOutgoingChannel_closed_cached() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, DURATION);
        forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, DURATION);
        verify(dao, times(1)).getEventsWithOutgoingChannel(CHANNEL_ID, DURATION);
    }

    @Test
    void getEventsWithIncomingChannel_closed_cached() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, DURATION);
        forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, DURATION);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID, DURATION);
    }

    @Test
    void getEventsWithOutgoingChannel_closed_with_max_age_just_after_channel_close_height() {
        Duration duration = getDurationShortlyAfterClose();
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, duration)).isEqualTo(List.of());
        verify(dao).getEventsWithOutgoingChannel(any(), any());
    }

    @Test
    void getEventsWithIncomingChannel_closed_with_max_age_just_after_channel_close_height() {
        Duration duration = getDurationShortlyAfterClose();
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, duration)).isEqualTo(List.of());
        verify(dao).getEventsWithIncomingChannel(any(), any());
    }

    @Test
    void getEventsWithOutgoingChannel_closed_with_max_age_long_after_channel_close_height() {
        Duration duration = getDurationLongAfterClose();
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, duration)).isEqualTo(List.of());
        verify(dao, never()).getEventsWithOutgoingChannel(any(), any());
    }

    @Test
    void getEventsWithIncomingChannel_closed_with_max_age_long_after_channel_close_height() {
        Duration duration = getDurationLongAfterClose();
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, duration)).isEqualTo(List.of());
        verify(dao, never()).getEventsWithIncomingChannel(any(), any());
    }

    @Test
    void getEventsWithOutgoingChannel() {
        Duration maxAge = Duration.ofDays(123);
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(FORWARDING_EVENT));
        assertThat(forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, maxAge))
                .containsExactly(FORWARDING_EVENT);
    }

    @Test
    void getEventsWithOutgoingChannel_cached() {
        Duration maxAge = Duration.ofDays(123);
        forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, maxAge);
        forwardingEventsService.getEventsWithOutgoingChannel(CHANNEL_ID, maxAge);
        verify(dao, times(1)).getEventsWithOutgoingChannel(CHANNEL_ID, maxAge);
    }

    @Test
    void getEventsWithIncomingChannel() {
        Duration maxAge = Duration.ofDays(123);
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(FORWARDING_EVENT));
        assertThat(forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, maxAge))
                .containsExactly(FORWARDING_EVENT);
    }

    @Test
    void getEventsWithIncomingChannel_cached() {
        Duration maxAge = Duration.ofDays(123);
        forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, maxAge);
        forwardingEventsService.getEventsWithIncomingChannel(CHANNEL_ID, maxAge);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID, maxAge);
    }

    private Duration getDurationLongAfterClose() {
        int blocks = BLOCK_HEIGHT - CLOSED_CHANNEL.getCloseHeight();
        double daysWithTenMinutesPerBlock = blocks * 10.0 / 60 / 24;
        return Duration.ofDays((int) (daysWithTenMinutesPerBlock * 0.49));
    }

    private Duration getDurationShortlyAfterClose() {
        int blocks = BLOCK_HEIGHT - CLOSED_CHANNEL.getCloseHeight();
        double daysWithTenMinutesPerBlock = blocks * 10.0 / 60 / 24;
        return Duration.ofDays((int) (daysWithTenMinutesPerBlock * 0.9));
    }
}