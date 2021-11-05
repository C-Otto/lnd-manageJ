package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;

public class ForwardAttemptFixtures {
    public static final ForwardAttempt FORWARD_ATTEMPT = ForwardAttempt.builder()
            .withIncomingTimelock(1)
            .withOutgoingTimelock(2)
            .withHtlcDetails(HTLC_DETAILS)
            .withOutgoingAmount(200)
            .withIncomingAmount(100)
            .build();
}
