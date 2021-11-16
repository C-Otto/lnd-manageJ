package de.cotto.lndmanagej.model;

public class ChannelPointFixtures {
    public static final String TRANSACTION_HASH = "abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    public static final String TRANSACTION_HASH_2 = "abc111abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";
    public static final int OUTPUT = 1;
    public static final int OUTPUT_2 = 123;
    public static final ChannelPoint CHANNEL_POINT = ChannelPoint.create(TRANSACTION_HASH + ":" + OUTPUT);
    public static final ChannelPoint CHANNEL_POINT_2 = ChannelPoint.create(TRANSACTION_HASH + ":" + OUTPUT_2);
    public static final ChannelPoint CHANNEL_POINT_3 = ChannelPoint.create(TRANSACTION_HASH_2 + ":" + OUTPUT);
}
