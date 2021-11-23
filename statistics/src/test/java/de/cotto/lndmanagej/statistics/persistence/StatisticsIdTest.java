package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.statistics.StatisticsFixtures.TIMESTAMP;
import static org.assertj.core.api.Assertions.assertThat;

class StatisticsIdTest {

    private static final long CHANNEL_ID_LONG = CHANNEL_ID.getShortChannelId();
    private static final long TIMESTAMP_LONG = TIMESTAMP.toEpochSecond(ZoneOffset.UTC);

    private final StatisticsId statisticsId = new StatisticsId(CHANNEL_ID_LONG, TIMESTAMP_LONG);

    @Test
    void test_types() {
        assertThat(statisticsId.channelId()).isEqualTo(CHANNEL_ID_LONG);
        assertThat(statisticsId.timestamp()).isEqualTo(TIMESTAMP_LONG);
    }
}