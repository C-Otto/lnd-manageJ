package de.cotto.lndmanagej.transactions.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcTransactions;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.TransactionDao;
import de.cotto.lndmanagej.transactions.download.TransactionProvider;
import de.cotto.lndmanagej.transactions.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class TransactionService {
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(1);
    private final TransactionDao transactionDao;
    private final TransactionProvider transactionProvider;
    private final GrpcTransactions grpcTransactions;
    private final LoadingCache<TransactionHash, Optional<Boolean>> hashIsKnownCache;

    public TransactionService(
            TransactionDao transactionDao,
            TransactionProvider transactionProvider,
            GrpcTransactions grpcTransactions
    ) {
        this.transactionDao = transactionDao;
        this.transactionProvider = transactionProvider;
        this.grpcTransactions = grpcTransactions;
        hashIsKnownCache = new CacheBuilder()
                .withExpiry(CACHE_EXPIRY)
                .withMaximumSize(1_000)
                .build(this::isKnownByLndWithoutCache);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isKnownByLnd(TransactionHash transactionHash) {
        return hashIsKnownCache.get(transactionHash);
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    public Optional<Boolean> isKnownByLndWithoutCache(TransactionHash transactionHash) {
        Transaction transaction = getTransaction(transactionHash).orElse(null);
        if (transaction == null) {
            return Optional.empty();
        }
        int blockHeight = transaction.blockHeight();
        Set<TransactionHash> knownTransactionsInBlock = grpcTransactions.getKnownTransactionHashesInBlock(blockHeight)
                .orElse(null);
        if (knownTransactionsInBlock == null) {
            return Optional.empty();
        }
        return Optional.of(knownTransactionsInBlock.contains(transactionHash));
    }

    public Optional<Transaction> getTransaction(TransactionHash transactionHash) {
        Optional<Transaction> persistedTransaction = transactionDao.getTransaction(transactionHash);
        if (persistedTransaction.isPresent()) {
            return persistedTransaction;
        }
        return downloadAndPersist(transactionHash);
    }

    public boolean isKnown(TransactionHash transactionHash) {
        return transactionDao.getTransaction(transactionHash).isPresent();
    }

    public boolean isUnknown(TransactionHash transactionHash) {
        return !isKnown(transactionHash);
    }

    private Optional<Transaction> downloadAndPersist(TransactionHash transactionHash) {
        Optional<Transaction> optionalTransaction = transactionProvider.get(transactionHash);
        optionalTransaction.ifPresent(transactionDao::saveTransaction);
        return optionalTransaction;
    }
}
