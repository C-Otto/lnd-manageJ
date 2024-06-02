package de.cotto.lndmanagej.model;

public record FeeRateInformation(
        Coins baseFeeLocal,
        long feeRateLocal,
        Coins inboundBaseFeeLocal,
        long inboundFeeRateLocal,
        Coins baseFeeRemote,
        long feeRateRemote,
        Coins inboundBaseFeeRemote,
        long inboundFeeRateRemote
) {
    private FeeRateInformation(Policy localPolicy, Policy remotePolicy) {
        this(
                localPolicy.baseFee(),
                localPolicy.feeRate(),
                localPolicy.inboundBaseFee(),
                localPolicy.inboundFeeRate(),
                remotePolicy.baseFee(),
                remotePolicy.feeRate(),
                remotePolicy.inboundBaseFee(),
                remotePolicy.inboundFeeRate()
        );
    }

    public static FeeRateInformation fromPolicies(PoliciesForLocalChannel policies) {
        return new FeeRateInformation(policies.local(), policies.remote());
    }
}
