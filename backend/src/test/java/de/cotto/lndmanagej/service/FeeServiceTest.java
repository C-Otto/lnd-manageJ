package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.statistics.ForwardingEventsDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    @InjectMocks
    private FeeService feeService;

    @Mock
    private ForwardingEventsDao dao;

    @Test
    void getEarnedFeesForChannel() {
        when(dao.getEventsWithOutgoingChannel(CHANNEL_ID)).thenReturn(List.of(FORWARDING_EVENT, FORWARDING_EVENT_2));
        assertThat(feeService.getEarnedFeesForChannel(CHANNEL_ID)).isEqualTo(Coins.ofMilliSatoshis(101));
    }

    @Test
    void getEarnedFeesForChannel_no_forward() {
        assertThat(feeService.getEarnedFeesForChannel(CHANNEL_ID)).isEqualTo(Coins.NONE);
    }
}