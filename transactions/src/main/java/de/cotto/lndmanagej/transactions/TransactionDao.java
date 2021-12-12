package de.cotto.lndmanagej.transactions;

import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.model.Transaction;

import java.util.Optional;

public interface TransactionDao {
    Optional<Transaction> getTransaction(TransactionHash transactionHash);

    void saveTransaction(Transaction transaction);
}
