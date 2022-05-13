package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policy;

public record PolicyDto(
        long feeRatePpm,
        long baseFeeMilliSat,
        boolean enabled
) {
    public static PolicyDto createFromModel(Policy policy) {
        return new PolicyDto(
                policy.feeRate(),
                policy.baseFee().milliSatoshis(),
                policy.enabled()
        );
    }
}
