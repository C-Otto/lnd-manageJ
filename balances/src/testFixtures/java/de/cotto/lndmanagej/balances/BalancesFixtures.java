package de.cotto.lndmanagej.balances;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;

public class BalancesFixtures {
    public static final LocalDateTime TIMESTAMP = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    public static final Balances BALANCES =
            new Balances(TIMESTAMP, CHANNEL_ID, BALANCE_INFORMATION);
    public static final Balances BALANCES_OLD =
            new Balances(TIMESTAMP.minusSeconds(1), CHANNEL_ID, BALANCE_INFORMATION_2);
}
