package de.cotto.lndmanagej.model;

public class ChannelIdFixtures {
    public static final String CHANNEL_ID_COMPACT = "712345:123:1";
    public static final String CHANNEL_ID_COMPACT_2 = "799999:456:2";
    public static final String CHANNEL_ID_COMPACT_3 = "799999:456:3";
    public static final String CHANNEL_ID_COMPACT_4 = "799999:456:4";
    public static final ChannelId CHANNEL_ID = ChannelId.fromCompactForm(CHANNEL_ID_COMPACT);
    public static final ChannelId CHANNEL_ID_2 = ChannelId.fromCompactForm(CHANNEL_ID_COMPACT_2);
    public static final ChannelId CHANNEL_ID_3 = ChannelId.fromCompactForm(CHANNEL_ID_COMPACT_3);
    public static final ChannelId CHANNEL_ID_4 = ChannelId.fromCompactForm(CHANNEL_ID_COMPACT_4);
    public static final long CHANNEL_ID_SHORT = CHANNEL_ID.getShortChannelId();
    public static final long CHANNEL_ID_2_SHORT = CHANNEL_ID_2.getShortChannelId();
    public static final long CHANNEL_ID_3_SHORT = CHANNEL_ID_3.getShortChannelId();
    public static final long CHANNEL_ID_4_SHORT = CHANNEL_ID_4.getShortChannelId();
}
