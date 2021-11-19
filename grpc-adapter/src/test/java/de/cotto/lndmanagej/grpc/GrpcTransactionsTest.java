package de.cotto.lndmanagej.grpc;

import lnrpc.Transaction;
import lnrpc.TransactionDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcTransactionsTest {
    private static final int BLOCK_HEIGHT = 123_456;
    private static final String HASH = "abc";
    private static final String HASH_2 = "def";
    private static final Transaction LND_TRANSACTION = Transaction.newBuilder().setTxHash(HASH).build();
    private static final Transaction LND_TRANSACTION_2 = Transaction.newBuilder().setTxHash(HASH_2).build();
    private static final TransactionDetails LND_TRANSACTION_DETAILS_EMPTY = TransactionDetails.newBuilder().build();
    private static final TransactionDetails LND_TRANSACTION_DETAILS =
            TransactionDetails.newBuilder()
                    .addTransactions(LND_TRANSACTION)
                    .addTransactions(LND_TRANSACTION_2)
                    .build();

    @InjectMocks
    private GrpcTransactions grpcTransactions;

    @Mock
    private GrpcService grpcService;

    @Test
    void uses_block_height() {
        grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT);
        verify(grpcService).getTransactionsInBlock(BLOCK_HEIGHT);
    }

    @Test
    void empty_for_empty() {
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).isEmpty();
    }

    @Test
    void empty_set() {
        when(grpcService.getTransactionsInBlock(anyInt())).thenReturn(Optional.of(LND_TRANSACTION_DETAILS_EMPTY));
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).contains(Set.of());
    }

    @Test
    void contains_hash() {
        when(grpcService.getTransactionsInBlock(anyInt())).thenReturn(Optional.of(LND_TRANSACTION_DETAILS));
        assertThat(grpcTransactions.getKnownTransactionHashesInBlock(BLOCK_HEIGHT)).contains(Set.of(HASH, HASH_2));
    }
}