package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_WITH_NEGATIVE_INBOUND_FEES;

public class FeeRateInformationFixtures {
    public static final FeeRateInformation FEE_RATE_INFORMATION =
            FeeRateInformation.fromPolicies(POLICIES_FOR_LOCAL_CHANNEL);
    public static final FeeRateInformation FEE_RATE_INFORMATION_2 =
            FeeRateInformation.fromPolicies(POLICIES_WITH_NEGATIVE_INBOUND_FEES);
}
