package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OffChainCosts;

public record OffChainCostsDto(String rebalanceSource, String rebalanceTarget) {
    public OffChainCostsDto(Coins rebalanceSource, Coins rebalanceTarget) {
        this(
                String.valueOf(rebalanceSource.milliSatoshis()),
                String.valueOf(rebalanceTarget.milliSatoshis())
        );
    }

    public static OffChainCostsDto createFromModel(OffChainCosts offChainCosts) {
        return new OffChainCostsDto(offChainCosts.rebalanceSource(), offChainCosts.rebalanceTarget());
    }

}
