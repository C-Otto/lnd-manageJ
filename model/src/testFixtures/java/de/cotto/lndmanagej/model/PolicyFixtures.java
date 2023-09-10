package de.cotto.lndmanagej.model;

public class PolicyFixtures {
    public static final Policy POLICY_1 =
            new Policy(200, Coins.NONE, true, 40, Coins.ofSatoshis(159), Coins.ofSatoshis(10_000));
    public static final Policy POLICY_WITH_BASE_FEE =
            new Policy(200, Coins.ofMilliSatoshis(10), true, 40, Coins.ofSatoshis(159), Coins.ofSatoshis(10_000));
    public static final Policy POLICY_DISABLED =
            new Policy(200, Coins.NONE, false, 40, Coins.NONE, Coins.NONE);
    public static final Policy POLICY_2 =
            new Policy(300, Coins.ofMilliSatoshis(0), true, 144, Coins.ofSatoshis(159), Coins.ofSatoshis(22_222));

    public static final PoliciesForLocalChannel POLICIES_FOR_LOCAL_CHANNEL =
            new PoliciesForLocalChannel(POLICY_DISABLED, POLICY_2);
}
