package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.FeeConfiguration;

public record FeeConfigurationDto(
        long outgoingFeeRatePpm,
        long outgoingBaseFeeMilliSat,
        long incomingFeeRatePpm,
        long incomingBaseFeeMilliSat
) {
    public static final FeeConfigurationDto EMPTY =
            new FeeConfigurationDto(0, 0, 0, 0);

    public static FeeConfigurationDto createFrom(FeeConfiguration feeConfiguration) {
        return new FeeConfigurationDto(
                feeConfiguration.outgoingFeeRate(),
                feeConfiguration.outgoingBaseFee().milliSatoshis(),
                feeConfiguration.incomingFeeRate(),
                feeConfiguration.incomingBaseFee().milliSatoshis()
        );
    }
}
