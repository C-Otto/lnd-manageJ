package de.cotto.lndmanagej.statistics;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;

public class StatisticsFixtures {
    public static final LocalDateTime TIMESTAMP = LocalDateTime.now(ZoneOffset.UTC);
    public static final Statistics STATISTICS = new Statistics(TIMESTAMP, CHANNEL_ID, BALANCE_INFORMATION);
}
