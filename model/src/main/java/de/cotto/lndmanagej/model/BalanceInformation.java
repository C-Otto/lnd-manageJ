package de.cotto.lndmanagej.model;

public record BalanceInformation(
        Coins localBalance,
        Coins localReserve,
        Coins localAvailable,
        Coins remoteBalance,
        Coins remoteReserve,
        Coins remoteAvailable
) {

    public static final BalanceInformation EMPTY =
            new BalanceInformation(Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE);

    public BalanceInformation(Coins localBalance, Coins localReserve, Coins remoteBalance, Coins remoteReserve) {
        this(
                localBalance,
                localReserve,
                getAvailableBalance(localBalance, localReserve),
                remoteBalance,
                remoteReserve,
                getAvailableBalance(remoteBalance, remoteReserve)
        );
    }

    private static Coins getAvailableBalance(Coins localBalance, Coins localReserve) {
        Coins availableLocalBalance = localBalance.subtract(localReserve);
        if (availableLocalBalance.isNegative()) {
            return Coins.NONE;
        }
        return availableLocalBalance;
    }

    public BalanceInformation add(BalanceInformation other) {
        return new BalanceInformation(
                localBalance.add(other.localBalance),
                localReserve.add(other.localReserve),
                localAvailable.add(other.localAvailable),
                remoteBalance.add(other.remoteBalance),
                remoteReserve.add(other.remoteReserve),
                remoteAvailable.add(other.remoteAvailable)
        );
    }
}
