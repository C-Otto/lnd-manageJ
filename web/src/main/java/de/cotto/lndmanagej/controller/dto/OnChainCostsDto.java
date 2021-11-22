package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;

public record OnChainCostsDto(String openCosts, String closeCosts) {
    public OnChainCostsDto(Coins openCosts, Coins closeCosts) {
        this(String.valueOf(openCosts.satoshis()), String.valueOf(closeCosts.satoshis()));
    }
}
