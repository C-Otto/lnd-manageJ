package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_3;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_OLD;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    private static final Duration DEFAULT_MAX_DURATION = Duration.ofDays(365 * 1_000);
    private static final int BLOCK_HEIGHT = 700_000;

    @InjectMocks
    private FeeService feeService;

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
    void getFeeReportForChannel() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT_2, FORWARDING_EVENT_3));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(101), Coins.ofMilliSatoshis(5_001)));
    }

    @Test
    void getFeeReportForChannel_uses_default_day_limit() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT_OLD));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(100), Coins.NONE));
    }

    @Test
    void getFeeReportForChannel_with_day_limit() {
        Duration maxAge = Duration.ofDays(7);
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID, maxAge))
                .isEqualTo(new FeeReport(Coins.NONE, Coins.NONE));
        verify(dao).getEventsWithIncomingChannel(CHANNEL_ID, maxAge);
        verify(dao).getEventsWithOutgoingChannel(CHANNEL_ID, maxAge);
    }

    @Test
    void getFeeReportForChannel_cached() {
        feeService.getFeeReportForChannel(CHANNEL_ID);
        feeService.getFeeReportForChannel(CHANNEL_ID);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION);
    }

    @Test
    void getFeeReportForChannel_closed() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(100), Coins.NONE));
    }

    @Test
    void getFeeReportForChannel_closed_cached() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        feeService.getFeeReportForChannel(CHANNEL_ID);
        feeService.getFeeReportForChannel(CHANNEL_ID);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION);
    }

    @Test
    void getFeeReportForChannel_closed_with_max_age_just_after_channel_close_height() {
        int blocks = BLOCK_HEIGHT - CLOSED_CHANNEL.getCloseHeight();
        double daysWithTenMinutesPerBlock = blocks * 10.0 / 60 / 24;
        Duration duration = Duration.ofDays((int) (daysWithTenMinutesPerBlock * 0.9));
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID, duration)).isEqualTo(FeeReport.EMPTY);
        verify(dao).getEventsWithIncomingChannel(any(), any());
    }

    @Test
    void getFeeReportForChannel_closed_with_max_age_long_after_channel_close_height() {
        int blocks = BLOCK_HEIGHT - CLOSED_CHANNEL.getCloseHeight();
        double daysWithTenMinutesPerBlock = blocks * 10.0 / 60 / 24;
        Duration duration = Duration.ofDays((int) (daysWithTenMinutesPerBlock * 0.49));
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID, duration)).isEqualTo(FeeReport.EMPTY);
        verify(dao, never()).getEventsWithIncomingChannel(any(), any());
    }

    @Test
    void getFeeReportForChannel_no_forward() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION)).thenReturn(List.of());
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID, DEFAULT_MAX_DURATION)).thenReturn(List.of());
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID)).isEqualTo(FeeReport.EMPTY);
    }

    @Test
    void getFeeReportForPeer() {
        when(dao.getEventsWithOutgoingChannel(CLOSED_CHANNEL.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT));
        when(dao.getEventsWithOutgoingChannel(WAITING_CLOSE_CHANNEL_2.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of());
        when(dao.getEventsWithOutgoingChannel(LOCAL_OPEN_CHANNEL_3.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT_3));

        when(dao.getEventsWithIncomingChannel(CLOSED_CHANNEL.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT_3));
        when(dao.getEventsWithIncomingChannel(WAITING_CLOSE_CHANNEL_2.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of(FORWARDING_EVENT_2));
        when(dao.getEventsWithIncomingChannel(LOCAL_OPEN_CHANNEL_3.getId(), DEFAULT_MAX_DURATION))
                .thenReturn(List.of());

        when(channelService.getAllChannelsWith(PUBKEY))
                .thenReturn(Set.of(CLOSED_CHANNEL, WAITING_CLOSE_CHANNEL_2, LOCAL_OPEN_CHANNEL_3));

        assertThat(feeService.getFeeReportForPeer(PUBKEY))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(5_100), Coins.ofMilliSatoshis(5_001)));
    }

    @Test
    void getFeeReportForPeer_no_channel() {
        assertThat(feeService.getFeeReportForPeer(PUBKEY)).isEqualTo(FeeReport.EMPTY);
    }
}