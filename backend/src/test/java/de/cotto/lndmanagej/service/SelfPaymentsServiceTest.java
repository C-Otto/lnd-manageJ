package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsServiceTest {
    @InjectMocks
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private SelfPaymentsDao selfPaymentsDao;

    @Mock
    private ChannelService channelService;

    @Test
    void getSelfPaymentsFromChannel() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_closed() {
        when(channelService.isClosed(CHANNEL_ID)).thenReturn(true);
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_no_duplicates() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsDao.getSelfPaymentsFromChannel(LOCAL_OPEN_CHANNEL.getId())).thenReturn(List.of(SELF_PAYMENT));
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CLOSED_CHANNEL_2.getId())).thenReturn(List.of(SELF_PAYMENT_2));
        assertThat(selfPaymentsService.getSelfPaymentsFromPeer(PUBKEY)).containsExactly(SELF_PAYMENT, SELF_PAYMENT_2);
    }

    @Test
    void getSelfPaymentsToChannel() {
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsToChannel_closed() {
        when(channelService.isClosed(CHANNEL_ID)).thenReturn(true);
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsToPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsDao.getSelfPaymentsToChannel(LOCAL_OPEN_CHANNEL.getId())).thenReturn(List.of(SELF_PAYMENT));
        when(selfPaymentsDao.getSelfPaymentsToChannel(CLOSED_CHANNEL_2.getId())).thenReturn(List.of(SELF_PAYMENT_2));
        assertThat(selfPaymentsService.getSelfPaymentsToPeer(PUBKEY)).containsExactly(SELF_PAYMENT, SELF_PAYMENT_2);
    }
}