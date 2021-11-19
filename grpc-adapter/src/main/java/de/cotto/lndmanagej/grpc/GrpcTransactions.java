package de.cotto.lndmanagej.grpc;

import lnrpc.Transaction;
import lnrpc.TransactionDetails;
import org.springframework.stereotype.Component;

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
        return grpcService.getTransactionsInBlock(blockHeight)
                .map(TransactionDetails::getTransactionsList)
                .map(transactions ->
                        transactions.stream()
                                .map(Transaction::getTxHash)
                                .collect(Collectors.toSet())
                );
    }
}
