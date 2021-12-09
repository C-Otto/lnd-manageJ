package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;

public record OffChainCostsDto(String rebalanceSource, String rebalanceTarget) {
    public OffChainCostsDto(Coins rebalanceSource, Coins rebalanceTarget) {
        this(
                String.valueOf(rebalanceSource.milliSatoshis()),
                String.valueOf(rebalanceTarget.milliSatoshis())
        );
    }
}
