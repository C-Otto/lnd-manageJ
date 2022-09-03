package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.model.OnlineReportFixtures;
import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.junit.jupiter.api.Nested;
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
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class OnlinePeersServiceTest {
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneOffset.UTC);
    private static final int SEVEN_DAYS = 7;
    private static final int FOURTEEN_DAYS = 14;

    @InjectMocks
    private OnlinePeersService onlinePeersService;

    @Mock
    private OnlinePeersDao dao;

    @Nested
    class GetOnlineReport {
        @Test
        void with_time_if_given_status_matches_last_known_status() {
            when(dao.getMostRecentOnlineStatus(PUBKEY)).thenReturn(Optional.of(ONLINE_STATUS));
            mockFor12PercentOffline();
            assertThat(onlinePeersService.getOnlineReport(NODE_PEER)).isEqualTo(new OnlineReport(
                    true,
                    OnlineReportFixtures.TIMESTAMP,
                    88,
                    14,
                    1,
                    7
            ));
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
    }

    @Test
    void getDaysForOnlinePercentage() {
        assertThat(onlinePeersService.getDaysForOnlinePercentage()).isEqualTo(FOURTEEN_DAYS);
    }

    @Nested
    class GetOnlinePercentage {
        @Test
        void no_data() {
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isZero();
        }

        @Test
        void always_online() {
            ZonedDateTime early = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS))
                    .thenReturn(List.of(new OnlineStatus(true, early)));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isEqualTo(100);
        }

        @Test
        void always_offline() {
            ZonedDateTime early = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS))
                    .thenReturn(List.of(new OnlineStatus(false, early)));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isZero();
        }

        @Test
        void limited_data_offline() {
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS))
                    .thenReturn(List.of(new OnlineStatus(false, oneHourAgo)));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isZero();
        }

        @Test
        void limited_data_online() {
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS))
                    .thenReturn(List.of(new OnlineStatus(true, oneHourAgo)));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isEqualTo(100);
        }

        @Test
        void limited_data_online_then_offline() {
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(true, oneHourAgo),
                    new OnlineStatus(false, twoHoursAgo)
            ));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isCloseTo(50, offset(1));
        }

        @Test
        void many_state_changes() {
            ZonedDateTime fiveHoursAgo = NOW.minusHours(5);
            ZonedDateTime fourHoursAgo = NOW.minusHours(4);
            ZonedDateTime threeHoursAgo = NOW.minusHours(3);
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(true, oneHourAgo),
                    new OnlineStatus(false, twoHoursAgo),
                    new OnlineStatus(true, threeHoursAgo),
                    new OnlineStatus(false, fourHoursAgo),
                    new OnlineStatus(true, fiveHoursAgo)
            ));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isCloseTo(60, offset(1));
        }

        @Test
        void limited_data_offline_then_online() {
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(false, oneHourAgo),
                    new OnlineStatus(true, twoHoursAgo)
            ));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isCloseTo(50, offset(1));
        }

        @Test
        void cuts_off_old_data() {
            ZonedDateTime twoYearsAgo = NOW.minusYears(2);
            ZonedDateTime oneYearAgo = NOW.minusYears(1);
            ZonedDateTime thirteenDaysAgo = NOW.minusDays(6);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(true, thirteenDaysAgo),
                    new OnlineStatus(false, oneYearAgo),
                    new OnlineStatus(true, twoYearsAgo)
            ));
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isCloseTo(42, offset(1));
        }

        @Test
        void is_rounded() {
            mockFor12PercentOffline();
            assertThat(onlinePeersService.getOnlinePercentage(PUBKEY)).isCloseTo(88, offset(1));
        }
    }

    @Test
    void getDaysForChanges() {
        assertThat(onlinePeersService.getDaysForChanges()).isEqualTo(SEVEN_DAYS);
    }

    @Nested
    class GetChanges {
        @Test
        void getChanges_no_data() {
            assertThat(onlinePeersService.getChanges(PUBKEY)).isZero();
        }

        @Test
        void getChanges_one_old_entry() {
            ZonedDateTime oneYearAgo = NOW.minusYears(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(true, oneYearAgo)
            ));
            assertThat(onlinePeersService.getChanges(PUBKEY)).isZero();
        }

        @Test
        void getChanges_one_recent_entry() {
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(true, oneHourAgo)
            ));
            assertThat(onlinePeersService.getChanges(PUBKEY)).isZero();
        }

        @Test
        void getChanges_two_recent_entries() {
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(false, oneHourAgo),
                    new OnlineStatus(true, twoHoursAgo)
            ));
            assertThat(onlinePeersService.getChanges(PUBKEY)).isOne();
        }

        @Test
        void getChanges_many_recent_entries() {
            ZonedDateTime fourHoursAgo = NOW.minusHours(4);
            ZonedDateTime threeHoursAgo = NOW.minusHours(3);
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(false, oneHourAgo),
                    new OnlineStatus(true, twoHoursAgo),
                    new OnlineStatus(false, threeHoursAgo),
                    new OnlineStatus(true, fourHoursAgo)
            ));
            assertThat(onlinePeersService.getChanges(PUBKEY)).isEqualTo(3);
        }

        @Test
        void getChanges_many_recent_entries_not_all_are_changes() {
            ZonedDateTime fourHoursAgo = NOW.minusHours(4);
            ZonedDateTime threeHoursAgo = NOW.minusHours(3);
            ZonedDateTime twoHoursAgo = NOW.minusHours(2);
            ZonedDateTime oneHourAgo = NOW.minusHours(1);
            when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(List.of(
                    new OnlineStatus(false, oneHourAgo),
                    new OnlineStatus(false, twoHoursAgo),
                    new OnlineStatus(false, threeHoursAgo),
                    new OnlineStatus(true, fourHoursAgo)
            ));
            assertThat(onlinePeersService.getChanges(PUBKEY)).isOne();
        }
    }

    private void assertVeryRecentSince(OnlineReport report) {
        ZonedDateTime since = report.since();
        assertThat(since).isAfter(NOW.minusSeconds(1));
        assertThat(since.getNano()).isZero();
    }

    private void mockFor12PercentOffline() {
        ZonedDateTime longAgo = NOW.minusDays(21);
        ZonedDateTime offlineSince = NOW.minusMinutes(2318);
        List<OnlineStatus> onlineStatusList = List.of(
                new OnlineStatus(false, offlineSince),
                new OnlineStatus(true, longAgo)
        );
        lenient().when(dao.getAllForPeerUpToAgeInDays(PUBKEY, SEVEN_DAYS)).thenReturn(onlineStatusList);
        when(dao.getAllForPeerUpToAgeInDays(PUBKEY, FOURTEEN_DAYS)).thenReturn(onlineStatusList);
    }
}
