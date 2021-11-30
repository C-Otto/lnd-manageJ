package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
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
    void getEarnedFeesForChannel() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).thenReturn(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
        assertThat(feeService.getEarnedFeesForChannel(CHANNEL_ID)).isEqualTo(Coins.ofMilliSatoshis(101));
    }

    @Test
    void getEarnedFeesForChannel_no_forward() {
        assertThat(feeService.getEarnedFeesForChannel(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }

    @Test
    void getEarnedFeesForPeer_no_channel() {
        assertThat(feeService.getEarnedFeesForPeer(PUBKEY)).isEqualTo(Coins.NONE);
    }

    @Test
    void getEarnedFeesForPeer() {
        when(dao.getEventsWithOutgoingChannel(CLOSED_CHANNEL.getId())).thenReturn(List.of(FORWARDING_EVENT_3));
        when(dao.getEventsWithOutgoingChannel(WAITING_CLOSE_CHANNEL_2.getId())).thenReturn(List.of(FORWARDING_EVENT));
        when(dao.getEventsWithOutgoingChannel(LOCAL_OPEN_CHANNEL_3.getId())).thenReturn(List.of(FORWARDING_EVENT_2));
        when(channelService.getAllChannelsWith(PUBKEY))
                .thenReturn(Set.of(CLOSED_CHANNEL, WAITING_CLOSE_CHANNEL_2, LOCAL_OPEN_CHANNEL_3));
        assertThat(feeService.getEarnedFeesForPeer(PUBKEY)).isEqualTo(Coins.ofMilliSatoshis(5_101));
    }
}