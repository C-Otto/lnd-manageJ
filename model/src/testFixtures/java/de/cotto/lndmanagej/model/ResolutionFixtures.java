package de.cotto.lndmanagej.model;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;

public class ResolutionFixtures {
    private static final String CLAIMED = "CLAIMED";
    private static final String TIMEOUT = "TIMEOUT";
    private static final String COMMIT = "COMMIT";
    private static final String FIRST_STAGE = "FIRST_STAGE";
    private static final String INCOMING_HTLC = "INCOMING_HTLC";
    private static final String OUTGOING_HTLC = "OUTGOING_HTLC";
    private static final String ANCHOR = "ANCHOR";

    public static final Resolution RESOLUTION_EMPTY =
            new Resolution(Optional.empty(), "x", "y");
    public static final Resolution INCOMING_HTLC_CLAIMED =
            new Resolution(Optional.of(TRANSACTION_HASH_3), INCOMING_HTLC, CLAIMED);
    public static final Resolution INCOMING_HTLC_TIMEOUT =
            new Resolution(Optional.of(TRANSACTION_HASH_3), INCOMING_HTLC, TIMEOUT);
    public static final Resolution OUTGOING_HTLC_CLAIMED =
            new Resolution(Optional.of(TRANSACTION_HASH_3), OUTGOING_HTLC, CLAIMED);
    public static final Resolution OUTGOING_HTLC_TIMEOUT =
            new Resolution(Optional.of(TRANSACTION_HASH_3), OUTGOING_HTLC, TIMEOUT);
    public static final Resolution OUTGOING_HTLC_FIRST_STAGE =
            new Resolution(Optional.of(TRANSACTION_HASH_3), OUTGOING_HTLC, FIRST_STAGE);
    public static final Resolution COMMIT_CLAIMED =
            new Resolution(Optional.of(TRANSACTION_HASH_3), COMMIT, CLAIMED);
    public static final Resolution ANCHOR_CLAIMED =
            new Resolution(Optional.of(TRANSACTION_HASH_3), ANCHOR, CLAIMED);
}
