package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.BalanceInformation;

public record BalanceInformationModel(
        long localBalanceSat,
        long localReserveSat,
        long localAvailableSat,
        long remoteBalanceSat,
        long remoteReserveSat,
        long remoteAvailableSat
) {

    public static final BalanceInformationModel EMPTY = createFromModel(BalanceInformation.EMPTY);

    public static BalanceInformationModel createFromModel(BalanceInformation balanceInformation) {
        return new BalanceInformationModel(
                balanceInformation.localBalance().satoshis(),
                balanceInformation.localReserve().satoshis(),
                balanceInformation.localAvailable().satoshis(),
                balanceInformation.remoteBalance().satoshis(),
                balanceInformation.remoteReserve().satoshis(),
                balanceInformation.remoteAvailable().satoshis()
        );
    }

    public double getOutboundPercentage() {
        long outbound = localBalanceSat();
        long routableCapacity = outbound + remoteBalanceSat();
        return (1.0 * outbound / routableCapacity) * 100;
    }

    public long getRoutableCapacity() {
        return remoteBalanceSat() + localBalanceSat();
    }

    public double getInboundPercentage() {
        return 100 - getOutboundPercentage();
    }
}
