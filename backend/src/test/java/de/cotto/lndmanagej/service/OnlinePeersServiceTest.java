package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePeersServiceTest {
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneOffset.UTC);

    @InjectMocks
    private OnlinePeersService onlinePeersService;

    @Mock
    private OnlinePeersDao dao;

    @Test
    void with_time_if_given_status_matches_last_known_status() {
        when(dao.getMostRecentOnlineStatus(PUBKEY)).thenReturn(Optional.of(ONLINE_STATUS));
        mockFor23PercentOffline();
        assertThat(onlinePeersService.getOnlineReport(NODE_PEER)).isEqualTo(ONLINE_REPORT);
    }

    @Test
    void with_current_time_if_given_status_does_not_match_last_known_status() {
        when(dao.getMostRecentOnlineStatus(PUBKEY)).thenReturn(Optional.of(ONLINE_STATUS_OFFLINE));
        OnlineReport report = onlinePeersService.getOnlineReport(NODE_PEER);
        assertThat(report.online()).isTrue();
        assertVeryRecentSince(report);
    }

    @Test
    void with_current_time_if_no_persisted_status_known() {
        OnlineReport report = onlinePeersService.getOnlineReport(NODE);
        assertThat(report.online()).isFalse();
        assertVeryRecentSince(report);
    }

    @Test
    void with_current_state_if_no_persisted_status_known() {
        OnlineReport report = onlinePeersService.getOnlineReport(NODE_PEER);
        assertThat(report.online()).isTrue();
        assertVeryRecentSince(report);
    }

    @Test
    void getOnlinePercentageLastWeek_no_data() {
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isZero();
    }

    @Test
    void getOnlinePercentageLastWeek_always_online() {
        ZonedDateTime early = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(new OnlineStatus(true, early)));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isEqualTo(100);
    }

    @Test
    void getOnlinePercentageLastWeek_always_offline() {
        ZonedDateTime early = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(new OnlineStatus(false, early)));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isZero();
    }

    @Test
    void getOnlinePercentageLastWeek_limited_data_offline() {
        ZonedDateTime oneHourAgo = NOW.minusHours(1);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(new OnlineStatus(false, oneHourAgo)));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isZero();
    }

    @Test
    void getOnlinePercentageLastWeek_limited_data_online() {
        ZonedDateTime oneHourAgo = NOW.minusHours(1);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(new OnlineStatus(true, oneHourAgo)));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isEqualTo(100);
    }

    @Test
    void getOnlinePercentageLastWeek_limited_data_online_then_offline() {
        ZonedDateTime twoHoursAgo = NOW.minusHours(2);
        ZonedDateTime oneHourAgo = NOW.minusHours(1);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(
                new OnlineStatus(true, oneHourAgo),
                new OnlineStatus(false, twoHoursAgo)
        ));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isCloseTo(50, offset(1));
    }

    @Test
    void getOnlinePercentageLastWeek_many_state_changes() {
        ZonedDateTime fiveHoursAgo = NOW.minusHours(5);
        ZonedDateTime fourHoursAgo = NOW.minusHours(4);
        ZonedDateTime threeHoursAgo = NOW.minusHours(3);
        ZonedDateTime twoHoursAgo = NOW.minusHours(2);
        ZonedDateTime oneHourAgo = NOW.minusHours(1);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(
                new OnlineStatus(true, oneHourAgo),
                new OnlineStatus(false, twoHoursAgo),
                new OnlineStatus(true, threeHoursAgo),
                new OnlineStatus(false, fourHoursAgo),
                new OnlineStatus(true, fiveHoursAgo)
        ));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isCloseTo(60, offset(1));
    }

    @Test
    void getOnlinePercentageLastWeek_limited_data_offline_then_online() {
        ZonedDateTime twoHoursAgo = NOW.minusHours(2);
        ZonedDateTime oneHourAgo = NOW.minusHours(1);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(
                new OnlineStatus(false, oneHourAgo),
                new OnlineStatus(true, twoHoursAgo)
        ));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isCloseTo(50, offset(1));
    }

    @Test
    void getOnlinePercentageLastWeek_cuts_off_old_data() {
        ZonedDateTime twoYearsAgo = NOW.minusYears(2);
        ZonedDateTime oneYearAgo = NOW.minusYears(1);
        ZonedDateTime thirteenDaysAgo = NOW.minusDays(6);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(
                new OnlineStatus(true, thirteenDaysAgo),
                new OnlineStatus(false, oneYearAgo),
                new OnlineStatus(true, twoYearsAgo)
        ));
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isCloseTo(85, offset(1));
    }

    @Test
    void getOnlinePercentageLastWeek_is_rounded() {
        mockFor23PercentOffline();
        assertThat(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).isCloseTo(77, offset(1));
    }

    private void assertVeryRecentSince(OnlineReport report) {
        ZonedDateTime since = report.since();
        assertThat(since).isAfter(NOW.minusSeconds(1));
        assertThat(since.getNano()).isZero();
    }

    private void mockFor23PercentOffline() {
        ZonedDateTime longAgo = NOW.minusDays(14);
        ZonedDateTime offlineSince = NOW.minusMinutes(2318);
        when(dao.getAllForPeer(PUBKEY)).thenReturn(List.of(
                new OnlineStatus(false, offlineSince),
                new OnlineStatus(true, longAgo)
        ));
    }
}