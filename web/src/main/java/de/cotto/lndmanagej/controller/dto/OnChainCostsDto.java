package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.OnChainCosts;

public record OnChainCostsDto(String openCosts, String closeCosts, String sweepCosts) {
    public static OnChainCostsDto createFromModel(OnChainCosts onChainCosts) {
        long openSatoshi = onChainCosts.open().satoshis();
        long closeSatoshi = onChainCosts.close().satoshis();
        long sweepSatoshi = onChainCosts.sweep().satoshis();
        return new OnChainCostsDto(
                String.valueOf(openSatoshi),
                String.valueOf(closeSatoshi),
                String.valueOf(sweepSatoshi)
        );
    }
}
