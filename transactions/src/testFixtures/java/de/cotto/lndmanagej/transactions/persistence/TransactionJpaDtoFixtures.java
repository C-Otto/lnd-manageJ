package de.cotto.lndmanagej.transactions.persistence;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;

public class TransactionJpaDtoFixtures {
    public static final TransactionJpaDto TRANSACTION_JPA_DTO;

    static {
        TRANSACTION_JPA_DTO = new TransactionJpaDto();
        TRANSACTION_JPA_DTO.setHash(TRANSACTION_HASH.getHash());
        TRANSACTION_JPA_DTO.setBlockHeight(BLOCK_HEIGHT);
        TRANSACTION_JPA_DTO.setPositionInBlock(POSITION_IN_BLOCK);
        TRANSACTION_JPA_DTO.setFees(FEES.satoshis());
    }
}
