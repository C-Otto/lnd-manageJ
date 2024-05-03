package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policy;

public record PolicyDto(
        long feeRatePpm,
        String baseFeeMilliSat,
        long inboundFeeRatePpm,
        String inboundBaseFeeMilliSat,
        boolean enabled,
        int timeLockDelta,
        String minHtlcMilliSat,
        String maxHtlcMilliSat
) {
    public static PolicyDto createFromModel(Policy policy) {
        return new PolicyDto(
                policy.feeRate(),
                String.valueOf(policy.baseFee().milliSatoshis()),
                policy.inboundFeeRate(),
                String.valueOf(policy.inboundBaseFee().milliSatoshis()),
                policy.enabled(),
                policy.timeLockDelta(),
                String.valueOf(policy.minHtlc().milliSatoshis()),
                String.valueOf(policy.maxHtlc().milliSatoshis())
        );
    }
}
