package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.statistics.ForwardingEventsDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    @InjectMocks
    private FeeService feeService;

    @Mock
    private ForwardingEventsDao dao;

    @Mock
    private ChannelService channelService;

    @Test
    void getFeeReportForChannel() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).thenReturn(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID)).thenReturn(List.of(FORWARDING_EVENT_2, FORWARDING_EVENT_3));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(101), Coins.ofMilliSatoshis(5_001)));
    }

    @Test
    void getFeeReportForChannel_cached() {
        feeService.getFeeReportForChannel(CHANNEL_ID);
        feeService.getFeeReportForChannel(CHANNEL_ID);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID);
    }

    @Test
    void getFeeReportForChannel_closed() {
        when(channelService.isClosed(CHANNEL_ID)).thenReturn(true);
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).thenReturn(List.of(FORWARDING_EVENT));
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(100), Coins.NONE));
    }

    @Test
    void getFeeReportForChannel_closed_cached() {
        when(channelService.isClosed(CHANNEL_ID)).thenReturn(true);
        feeService.getFeeReportForChannel(CHANNEL_ID);
        feeService.getFeeReportForChannel(CHANNEL_ID);
        verify(dao, times(1)).getEventsWithIncomingChannel(CHANNEL_ID);
    }

    @Test
    void getFeeReportForChannel_no_forward() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).thenReturn(List.of());
        when(dao.getEventsWithIncomingChannel(CHANNEL_ID)).thenReturn(List.of());
        assertThat(feeService.getFeeReportForChannel(CHANNEL_ID)).isEqualTo(new FeeReport(Coins.NONE, Coins.NONE));
    }

    @Test
    void getFeeReportForPeer() {
        when(dao.getEventsWithOutgoingChannel(CLOSED_CHANNEL.getId())).thenReturn(List.of(FORWARDING_EVENT));
        when(dao.getEventsWithOutgoingChannel(WAITING_CLOSE_CHANNEL_2.getId())).thenReturn(List.of());
        when(dao.getEventsWithOutgoingChannel(LOCAL_OPEN_CHANNEL_3.getId())).thenReturn(List.of(FORWARDING_EVENT_3));

        when(dao.getEventsWithIncomingChannel(CLOSED_CHANNEL.getId())).thenReturn(List.of(FORWARDING_EVENT_3));
        when(dao.getEventsWithIncomingChannel(WAITING_CLOSE_CHANNEL_2.getId())).thenReturn(List.of(FORWARDING_EVENT_2));
        when(dao.getEventsWithIncomingChannel(LOCAL_OPEN_CHANNEL_3.getId())).thenReturn(List.of());

        when(channelService.getAllChannelsWith(PUBKEY))
                .thenReturn(Set.of(CLOSED_CHANNEL, WAITING_CLOSE_CHANNEL_2, LOCAL_OPEN_CHANNEL_3));

        assertThat(feeService.getFeeReportForPeer(PUBKEY))
                .isEqualTo(new FeeReport(Coins.ofMilliSatoshis(5_100), Coins.ofMilliSatoshis(5_001)));
    }

    @Test
    void getFeeReportForPeer_no_channel() {
        assertThat(feeService.getFeeReportForPeer(PUBKEY)).isEqualTo(new FeeReport(Coins.NONE, Coins.NONE));
    }
}