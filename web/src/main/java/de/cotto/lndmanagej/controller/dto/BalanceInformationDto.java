package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;

public record BalanceInformationDto(
        String localBalanceSat,
        String localReserveSat,
        String localAvailableSat,
        String remoteBalanceSat,
        String remoteReserveSat,
        String remoteAvailableSat
) {
    public static BalanceInformationDto createFromModel(BalanceInformation balanceInformation) {
        return new BalanceInformationDto(
                toString(balanceInformation.localBalance()),
                toString(balanceInformation.localReserve()),
                toString(balanceInformation.localAvailable()),
                toString(balanceInformation.remoteBalance()),
                toString(balanceInformation.remoteReserve()),
                toString(balanceInformation.remoteAvailable())
        );
    }

    private static String toString(Coins coins) {
        return String.valueOf(coins.satoshis());
    }
}
