package de.cotto.lndmanagej.grpc;

import lnrpc.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcTransactionsTest {
    private static final int BLOCK_HEIGHT = 123_456;
    private static final String HASH = "abc";
    private static final String HASH_2 = "def";
    private static final String HASH_3 = "ghi";
    private static final Transaction LND_TRANSACTION = transaction(HASH, BLOCK_HEIGHT);
    private static final Transaction LND_TRANSACTION_2 = transaction(HASH_2, BLOCK_HEIGHT);
    private static final Transaction LND_TRANSACTION_WRONG_BLOCK = transaction(HASH_3, BLOCK_HEIGHT + 1);

    @InjectMocks
    private GrpcTransactions grpcTransactions;

    @Mock
    private GrpcService grpcService;

    @Test
    void getKnownTransactionHashesInBlock_unknown() {
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).isEmpty();
    }

    @Test
    void getKnownTransactionHashesInBlock_empty() {
        when(grpcService.getTransactions()).thenReturn(Optional.of(List.of()));
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).contains(Set.of());
    }

    @Test
    void getKnownTransactionHashesInBlock() {
        when(grpcService.getTransactions()).thenReturn(Optional.of(
                List.of(LND_TRANSACTION, LND_TRANSACTION_2, LND_TRANSACTION_WRONG_BLOCK)
        ));
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).contains(Set.of(HASH, HASH_2));
    }

    private static Transaction transaction(String hash, int blockHeight) {
        return Transaction.newBuilder().setTxHash(hash).setBlockHeight(blockHeight).build();
    }
}