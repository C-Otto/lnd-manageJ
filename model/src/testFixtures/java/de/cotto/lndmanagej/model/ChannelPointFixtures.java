package de.cotto.lndmanagej.model;

public class ChannelPointFixtures {
    public static final String TRANSACTION_HASH = "abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    public static final int OUTPUT = 1;
    public static final ChannelPoint CHANNEL_POINT = ChannelPoint.create(TRANSACTION_HASH + ":" + OUTPUT);
}
