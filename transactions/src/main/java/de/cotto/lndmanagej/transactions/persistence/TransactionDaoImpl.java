package de.cotto.lndmanagej.transactions.persistence;

import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.TransactionDao;
import de.cotto.lndmanagej.transactions.model.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Transactional
public class TransactionDaoImpl implements TransactionDao {
    private final TransactionRepository transactionRepository;

    public TransactionDaoImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Optional<Transaction> getTransaction(TransactionHash transactionHash) {
        return transactionRepository.findById(transactionHash.getHash())
                .flatMap(TransactionJpaDto::toModel);
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(TransactionJpaDto.fromModel(transaction));
    }
}
