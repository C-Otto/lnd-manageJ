package de.cotto.lndmanagej.transactions.download;

import java.util.Optional;

public interface TransactionDetailsClient {
    Optional<? extends TransactionDto> getTransaction(String transactionHash);
}
