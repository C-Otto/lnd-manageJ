package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;

public class SettledForwardFixtures {
    public static final SettledForward SETTLED_FORWARD = new SettledForward(HTLC_DETAILS, FORWARD_ATTEMPT);
}
