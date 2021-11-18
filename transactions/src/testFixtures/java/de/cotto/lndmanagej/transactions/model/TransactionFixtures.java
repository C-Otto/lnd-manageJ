package de.cotto.lndmanagej.transactions.model;

import de.cotto.lndmanagej.model.Coins;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;

public class TransactionFixtures {
    public static final int BLOCK_HEIGHT = 700_123;
    public static final int POSITION_IN_BLOCK = 1234;
    public static final Coins FEES = Coins.ofSatoshis(124);
    public static final Coins FEES_2 = Coins.ofSatoshis(456);
    public static final Transaction TRANSACTION =
            new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, POSITION_IN_BLOCK, FEES);
    public static final Transaction TRANSACTION_2 =
            new Transaction(TRANSACTION_HASH_2, BLOCK_HEIGHT, POSITION_IN_BLOCK, FEES_2);
}
