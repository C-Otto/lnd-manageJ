package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsServiceTest {
    @InjectMocks
    private SelfPaymentsService selfPaymentsService;

    @Mock
    private SelfPaymentsDao selfPaymentsDao;

    @Test
    void getSelfPaymentsToChannel() {
        when(selfPaymentsDao.getSelfPaymentsToChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }

    @Test
    void getSelfPaymentsFromChannel_no_duplicates() {
        when(selfPaymentsDao.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT));
        assertThat(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(SELF_PAYMENT);
    }
}