package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
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
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsServiceTest {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);

    @InjectMocks
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private SelfPaymentsDao selfPaymentsDao;

    @Mock
    private ChannelService channelService;

    @Mock
    private OwnNodeService ownNodeService;

    @Test
    void getSelfPaymentsFromChannel() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_shared_with_another_channel() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID_3, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_4));
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID_4, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_4));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID_3)).containsExactly(SELF_PAYMENT_4);
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID_4)).isEmpty();
    }

    @Test
    void getSelfPaymentsFromChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(1);
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, maxAge)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_closed() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(1_000 + CLOSED_CHANNEL.getCloseHeight());
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_closed_long_time_ago() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(1_000 + CLOSED_CHANNEL.getCloseHeight());
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID, Duration.ofDays(1))).isEmpty();
        verify(selfPaymentsDao, never()).getSelfPaymentsFromChannel(any(), any());
    }

    @Test
    void getSelfPaymentsFromChannel_no_duplicates() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsDao.getSelfPaymentsFromChannel(LOCAL_OPEN_CHANNEL.getId(), DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT));
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CLOSED_CHANNEL_2.getId(), DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_2));
        assertThat(selfPaymentsService.getSelfPaymentsFromPeer(PUBKEY)).containsExactly(SELF_PAYMENT, SELF_PAYMENT_2);
    }

    @Test
    void getSelfPaymentsToChannel() {
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsToChannel_no_duplicates() {
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).hasSize(1);
    }

    @Test
    void getSelfPaymentsToChannel_shared_with_another_channel() {
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_4));
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID_2, DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_4));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT_4);
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID_2)).isEmpty();
    }

    @Test
    void getSelfPaymentsToChannel_with_max_age() {
        Duration maxAge = Duration.ofDays(23);
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID, maxAge)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID, maxAge)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsToChannel_closed() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(1_000 + CLOSED_CHANNEL.getCloseHeight());
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID, DEFAULT_MAX_AGE)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsToChannel_closed_long_time_ago() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(1_000 + CLOSED_CHANNEL.getCloseHeight());
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID, Duration.ofDays(1))).isEmpty();
        verify(selfPaymentsDao, never()).getSelfPaymentsToChannel(any(), any());
    }

    @Test
    void getSelfPaymentsToPeer() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_2));
        when(selfPaymentsDao.getSelfPaymentsToChannel(LOCAL_OPEN_CHANNEL.getId(), DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT_2));
        when(selfPaymentsDao.getSelfPaymentsToChannel(CLOSED_CHANNEL_2.getId(), DEFAULT_MAX_AGE))
                .thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToPeer(PUBKEY)).containsExactly(SELF_PAYMENT, SELF_PAYMENT_2);
    }
}
