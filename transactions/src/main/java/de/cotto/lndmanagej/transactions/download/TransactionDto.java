package de.cotto.lndmanagej.transactions.download;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.transactions.model.Transaction;

public class TransactionDto {
    private final String hash;
    private final int blockHeight;
    private final int positionInBlock;
    private final Coins fees;

    public TransactionDto(
            String hash,
            int blockHeight,
            int positionInBlock,
            Coins fees
    ) {
        this.hash = hash;
        this.blockHeight = blockHeight;
        this.positionInBlock = positionInBlock;
        this.fees = fees;
    }

    public Transaction toModel() {
        return new Transaction(hash, blockHeight, positionInBlock, fees);
    }
}
