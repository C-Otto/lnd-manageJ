package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.TransactionHash;
import lnrpc.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class GrpcTransactions {
    private final GrpcService grpcService;

    public GrpcTransactions(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Optional<Set<TransactionHash>> getKnownTransactionHashesInBlock(int blockHeight) {
        List<Transaction> transactionsInBlock = getTransactionsInBlock(blockHeight).orElse(null);
        if (transactionsInBlock == null) {
            return Optional.empty();
        }
        Set<TransactionHash> hashes = transactionsInBlock.stream()
                .map(Transaction::getTxHash)
                .map(TransactionHash::create)
                .collect(toSet());
        return Optional.of(hashes);
    }

    public Optional<List<Transaction>> getTransactionsInBlock(int blockHeight) {
        List<Transaction> transactions = grpcService.getTransactions().orElse(null);
        if (transactions == null) {
            return Optional.empty();
        }
        List<Transaction> filteredTransactions = transactions.stream()
                .filter(transaction -> transaction.getBlockHeight() == blockHeight)
                .toList();
        return Optional.of(filteredTransactions);
    }
}
