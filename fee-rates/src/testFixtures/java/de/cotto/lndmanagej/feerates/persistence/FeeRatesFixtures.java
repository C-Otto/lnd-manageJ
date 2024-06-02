package de.cotto.lndmanagej.feerates.persistence;

import de.cotto.lndmanagej.feerates.FeeRates;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FeeRateInformationFixtures.FEE_RATE_INFORMATION;
import static de.cotto.lndmanagej.model.FeeRateInformationFixtures.FEE_RATE_INFORMATION_2;

public class FeeRatesFixtures {
    public static final LocalDateTime TIMESTAMP = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    public static final FeeRates FEE_RATES =
            new FeeRates(TIMESTAMP, CHANNEL_ID, FEE_RATE_INFORMATION);
    public static final FeeRates FEE_RATES_OLD =
            new FeeRates(TIMESTAMP.minusSeconds(1), CHANNEL_ID, FEE_RATE_INFORMATION_2);
}
