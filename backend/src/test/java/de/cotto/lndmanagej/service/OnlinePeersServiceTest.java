package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.OnlineReport;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePeersServiceTest {
    @InjectMocks
    private OnlinePeersService onlinePeersService;

    @Mock
    private OnlinePeersDao dao;

    @Test
    void with_time_if_given_status_matches_last_known_status() {
        when(dao.getMostRecentOnlineStatus(PUBKEY)).thenReturn(Optional.of(ONLINE_STATUS));
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

    private void assertVeryRecentSince(OnlineReport report) {
        assertThat(report.since())
                .isAfter(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(1))
                .asString().hasSize(20);
    }
}