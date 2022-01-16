package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OnlineReportTest {
    @Test
    void online() {
        assertThat(ONLINE_REPORT.online()).isTrue();
    }

    @Test
    void since() {
        assertThat(ONLINE_REPORT.since()).isEqualTo(TIMESTAMP);
    }

    @Test
    void onlinePercentage() {
        assertThat(ONLINE_REPORT.onlinePercentage()).isEqualTo(77);
    }

    @Test
    void daysForOnlinePercentage() {
        assertThat(ONLINE_REPORT.daysForOnlinePercentage()).isEqualTo(14);
    }

    @Test
    void changes() {
        assertThat(ONLINE_REPORT.changes()).isEqualTo(5);
    }

    @Test
    void daysForChanges() {
        assertThat(ONLINE_REPORT.daysForChanges()).isEqualTo(7);
    }

    @Test
    void createFromOnlineStatus() {
        assertThat(OnlineReport.createFromStatus(
                ONLINE_STATUS,
                77,
                14,
                5,
                7
        )).isEqualTo(ONLINE_REPORT);
    }
}