package de.cotto.lndmanagej.transactions.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.TransactionHash;

public record Transaction(
        TransactionHash hash,
        int blockHeight,
        int positionInBlock,
        Coins fees
) {
}