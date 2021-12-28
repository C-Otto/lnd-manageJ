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
    void onlinePercentageLastWeek() {
        assertThat(ONLINE_REPORT.onlinePercentageLastWeek()).isEqualTo(77);
    }

    @Test
    void changesLastWeek() {
        assertThat(ONLINE_REPORT.changesLastWeek()).isEqualTo(5);
    }

    @Test
    void createFromOnlineStatus() {
        assertThat(OnlineReport.createFromStatus(ONLINE_STATUS, 77, 5))
                .isEqualTo(ONLINE_REPORT);
    }
}