package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;

public class ForwardFailureFixtures {
    public static final ForwardFailure FORWARD_FAILURE = new ForwardFailure(HTLC_DETAILS, FORWARD_ATTEMPT);
}
