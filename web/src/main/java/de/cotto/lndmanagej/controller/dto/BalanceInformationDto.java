package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;

public record BalanceInformationDto(
        String localBalance,
        String localReserve,
        String localAvailable,
        String remoteBalance,
        String remoteReserve,
        String remoteAvailable
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
