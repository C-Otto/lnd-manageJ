package de.cotto.lndmanagej.transactions.service;

import de.cotto.lndmanagej.transactions.TransactionDao;
import de.cotto.lndmanagej.transactions.download.TransactionProvider;
import de.cotto.lndmanagej.transactions.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionService {
    private final TransactionDao transactionDao;
    private final TransactionProvider transactionProvider;

    public TransactionService(
            TransactionDao transactionDao,
            TransactionProvider transactionProvider
    ) {
        this.transactionDao = transactionDao;
        this.transactionProvider = transactionProvider;
    }

    public Optional<Transaction> getTransaction(String transactionHash) {
        Optional<Transaction> persistedTransaction = transactionDao.getTransaction(transactionHash);
        if (persistedTransaction.isPresent()) {
            return persistedTransaction;
        }
        return downloadAndPersist(transactionHash);
    }

    private Optional<Transaction> downloadAndPersist(String transactionHash) {
        Optional<Transaction> optionalTransaction = transactionProvider.get(transactionHash);
        optionalTransaction.ifPresent(transactionDao::saveTransaction);
        return optionalTransaction;
    }
}
