package de.cotto.lndmanagej.transactions.model;

import de.cotto.lndmanagej.model.Coins;

public record Transaction(
        String hash,
        int blockHeight,
        int positionInBlock,
        Coins fees
) {
}