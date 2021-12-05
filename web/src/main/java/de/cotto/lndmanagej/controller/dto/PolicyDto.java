package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policy;

public record PolicyDto(
        long feeRatePpm,
        long baseFeeMilliSat,
        boolean enabled
) {
    public static final PolicyDto EMPTY =
            new PolicyDto(
                    0,
                    0,
                    false
            );

    public static PolicyDto createFromModel(Policy policy) {
        return new PolicyDto(
                policy.feeRate(),
                policy.baseFee().milliSatoshis(),
                policy.enabled()
        );
    }
}
