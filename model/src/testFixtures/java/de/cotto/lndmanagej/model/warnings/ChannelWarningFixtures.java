package de.cotto.lndmanagej.model.warnings;

public class ChannelWarningFixtures {
    public static final ChannelNumUpdatesWarning CHANNEL_NUM_UPDATES_WARNING = new ChannelNumUpdatesWarning(101_000L);
    public static final ChannelBalanceFluctuationWarning CHANNEL_BALANCE_FLUCTUATION_WARNING =
            new ChannelBalanceFluctuationWarning(2, 97, 7);
}
