package de.cotto.lndmanagej.transactions.download;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;

public class BlockcypherTransactionDtoFixtures {
    public static final BlockcypherTransactionDto BLOCKCYPHER_TRANSACTION =
            new BlockcypherTransactionDto(TRANSACTION_HASH, BLOCK_HEIGHT, POSITION_IN_BLOCK, FEES);
}
