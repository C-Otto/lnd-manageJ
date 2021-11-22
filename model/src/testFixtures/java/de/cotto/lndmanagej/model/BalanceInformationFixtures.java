package de.cotto.lndmanagej.model;

public class BalanceInformationFixtures {
    public static final Coins LOCAL_BALANCE = Coins.ofSatoshis(1_000);
    public static final Coins LOCAL_RESERVE = Coins.ofSatoshis(100);
    public static final Coins REMOTE_BALANCE = Coins.ofSatoshis(123);
    public static final Coins REMOTE_RESERVE = Coins.ofSatoshis(10);

    public static final Coins LOCAL_BALANCE_2 = Coins.ofSatoshis(1_000);
    public static final Coins LOCAL_RESERVE_2 = Coins.ofSatoshis(100);
    public static final Coins REMOTE_BALANCE_2 = Coins.ofSatoshis(123);
    public static final Coins REMOTE_RESERVE_2 = Coins.ofSatoshis(10);

    public static final BalanceInformation BALANCE_INFORMATION =
            new BalanceInformation(LOCAL_BALANCE, LOCAL_RESERVE, REMOTE_BALANCE, REMOTE_RESERVE);
    public static final BalanceInformation BALANCE_INFORMATION_2 =
            new BalanceInformation(LOCAL_BALANCE_2, LOCAL_RESERVE_2, REMOTE_BALANCE_2, REMOTE_RESERVE_2);
}
