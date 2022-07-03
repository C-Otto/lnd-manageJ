package de.cotto.lndmanagej.ui.controller.param;

public enum SortBy {
    DEFAULT_SORT,
    RATIO,
    ANNOUNCED,
    INBOUND,
    OUTBOUND,
    CAPACITY,
    LOCAL_BASE_FEE,
    REMOTE_BASE_FEE,
    LOCAL_FEE_RATE,
    REMOTE_FEE_RATE,
    ALIAS,
    RATING,
    CHANNEL_ID,
    NODE_ALIAS,
    NODE_RATING,
    PUBKEY,
    ONLINE;

    public static final String SORT_PARAM_KEY = "sort";

}
