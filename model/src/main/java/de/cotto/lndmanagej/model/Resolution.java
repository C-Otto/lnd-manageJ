package de.cotto.lndmanagej.model;

import java.util.Optional;

public record Resolution(Optional<TransactionHash> sweepTransaction, String resolutionType, String outcome) {

    private static final String FIRST_STAGE = "FIRST_STAGE";
    private static final String TIMEOUT = "TIMEOUT";
    private static final String CLAIMED = "CLAIMED";
    private static final String OUTGOING_HTLC = "OUTGOING_HTLC";
    private static final String INCOMING_HTLC = "INCOMING_HTLC";
    private static final String ANCHOR = "ANCHOR";
    private static final String COMMIT = "COMMIT";

    public boolean sweepTransactionClaimsFunds() {
        if (OUTGOING_HTLC.equals(resolutionType) && TIMEOUT.equals(outcome)) {
            return true;
        }
        if (OUTGOING_HTLC.equals(resolutionType) && FIRST_STAGE.equals(outcome)) {
            return true;
        }
        if (INCOMING_HTLC.equals(resolutionType) && CLAIMED.equals(outcome)) {
            return true;
        }
        if (ANCHOR.equals(resolutionType) && CLAIMED.equals(outcome)) {
            return true;
        }
        return COMMIT.equals(resolutionType) && CLAIMED.equals(outcome);
    }

    public boolean isClaimedAnchor() {
        return ANCHOR.equals(resolutionType) && CLAIMED.equals(outcome);
    }
}
