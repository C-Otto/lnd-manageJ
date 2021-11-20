package de.cotto.lndmanagej.grpc;

import lnrpc.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GrpcTransactions {
    private final GrpcService grpcService;

    public GrpcTransactions(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Optional<Set<String>> getKnownTransactionHashesInBlock(int blockHeight) {
        List<Transaction> transactionsInBlock = getTransactionsInBlock(blockHeight).orElse(null);
        if (transactionsInBlock == null) {
            return Optional.empty();
        }
        Set<String> hashes = transactionsInBlock.stream()
                .map(Transaction::getTxHash)
                .collect(Collectors.toSet());
        return Optional.of(hashes);
    }

    public Optional<List<Transaction>> getTransactionsInBlock(int blockHeight) {
        List<Transaction> transactions = grpcService.getTransactions().orElse(null);
        if (transactions == null) {
            return Optional.empty();
        }
        List<Transaction> filteredTransactions = transactions.stream()
                .filter(transaction -> transaction.getBlockHeight() == blockHeight)
                .collect(Collectors.toList());
        return Optional.of(filteredTransactions);
    }
}
