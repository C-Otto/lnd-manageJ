package de.cotto.lndmanagej.model;

public class PolicyFixtures {
    public static final Policy POLICY_1 = new Policy(100, Coins.ofMilliSatoshis(10), false);
    public static final Policy POLICY_2 = new Policy(222, Coins.ofMilliSatoshis(0), true);

    public static final Policies POLICIES = new Policies(POLICY_1, POLICY_2);
}
