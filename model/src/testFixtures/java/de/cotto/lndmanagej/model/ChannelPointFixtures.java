package de.cotto.lndmanagej.model;

public class ChannelPointFixtures {
    public static final TransactionHash TRANSACTION_HASH =
            TransactionHash.create("abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0");
    public static final TransactionHash TRANSACTION_HASH_2 =
            TransactionHash.create("abc111abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0");
    public static final TransactionHash TRANSACTION_HASH_3 =
            TransactionHash.create("abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0");
    public static final int OUTPUT = 1;
    public static final int OUTPUT_2 = 123;
    public static final ChannelPoint CHANNEL_POINT = ChannelPoint.create(TRANSACTION_HASH + ":" + OUTPUT);
    public static final ChannelPoint CHANNEL_POINT_2 = ChannelPoint.create(TRANSACTION_HASH + ":" + OUTPUT_2);
    public static final ChannelPoint CHANNEL_POINT_3 = ChannelPoint.create(TRANSACTION_HASH_2 + ":" + OUTPUT);
}
