package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policy;

public record PolicyDto(
        long feeRatePpm,
        String baseFeeMilliSat,
        boolean enabled,
        int timeLockDelta,
        String maxHtlcMilliSat
) {
    public static PolicyDto createFromModel(Policy policy) {
        return new PolicyDto(
                policy.feeRate(),
                String.valueOf(policy.baseFee().milliSatoshis()),
                policy.enabled(),
                policy.timeLockDelta(),
                String.valueOf(policy.maxHtlc().milliSatoshis())
        );
    }
}
