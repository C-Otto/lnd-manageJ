package de.cotto.lndmanagej.model;

public record BalanceInformation(
        Coins localBalance,
        Coins localReserve,
        Coins remoteBalance,
        Coins remoteReserve
) {

    public Coins availableLocalBalance() {
        Coins availableLocalBalance = localBalance().subtract(localReserve());
        if (availableLocalBalance.isNegative()) {
            return Coins.NONE;
        }
        return availableLocalBalance;
    }

    public Coins availableRemoteBalance() {
        Coins availableRemoteBalance = remoteBalance().subtract(remoteReserve());
        if (availableRemoteBalance.isNegative()) {
            return Coins.NONE;
        }
        return availableRemoteBalance;
    }
}
