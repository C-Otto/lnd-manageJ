package de.cotto.lndmanagej.transactions.model;

import de.cotto.lndmanagej.model.Coins;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;

public class TransactionFixtures {
    public static final int BLOCK_HEIGHT = 700_123;
    public static final int POSITION_IN_BLOCK = 1234;
    public static final Coins FEES = Coins.ofSatoshis(123);
    public static final Transaction TRANSACTION =
            new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, POSITION_IN_BLOCK, FEES);
}
