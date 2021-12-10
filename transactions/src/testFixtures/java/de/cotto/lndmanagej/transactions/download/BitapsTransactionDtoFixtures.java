package de.cotto.lndmanagej.transactions.download;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;

public class BitapsTransactionDtoFixtures {
    public static final BitapsTransactionDto BITAPS_TRANSACTION =
            new BitapsTransactionDto(TRANSACTION_HASH, BLOCK_HEIGHT, POSITION_IN_BLOCK, FEES);
}
