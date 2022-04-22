package de.cotto.lndmanagej.transactions.download;

import java.util.Optional;

public interface TransactionDetailsClient {
    Optional<? extends TransactionDto> getTransaction(String transactionHash);

    Optional<? extends TransactionDto> getTransactionTestnet(String transactionHash);
}
