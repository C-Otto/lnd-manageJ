package de.cotto.lndmanagej.transactions;

import de.cotto.lndmanagej.transactions.model.Transaction;

import java.util.Optional;

public interface TransactionDao {
    Optional<Transaction> getTransaction(String transactionHash);

    void saveTransaction(Transaction transaction);
}
