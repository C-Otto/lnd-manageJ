package de.cotto.lndmanagej.transactions.service;

import de.cotto.lndmanagej.grpc.GrpcTransactions;
import de.cotto.lndmanagej.transactions.TransactionDao;
import de.cotto.lndmanagej.transactions.download.TransactionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private TransactionProvider transactionProvider;

    @Mock
    private GrpcTransactions grpcTransactions;

    @Test
    void isKnownByLnd_unable_to_retrieve_transaction_details() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(transactionService.isKnownByLnd(TRANSACTION_HASH)).isEmpty();
        verify(grpcTransactions, never()).getKnownTransactionHashesInBlock(anyInt());
    }

    @Test
    void isKnownByLnd_unable_to_retrieve_lnd_transaction_list() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(transactionService.isKnownByLnd(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void isKnownByLnd_no_transaction_in_block() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        when(grpcTransactions.getKnownTransactionHashesInBlock(TRANSACTION.blockHeight()))
                .thenReturn(Optional.of(Set.of()));
        assertThat(transactionService.isKnownByLnd(TRANSACTION_HASH)).contains(false);
    }

    @Test
    void isKnownByLnd_only_other_transactions_in_block() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        when(grpcTransactions.getKnownTransactionHashesInBlock(TRANSACTION.blockHeight()))
                .thenReturn(Optional.of(Set.of(TRANSACTION_HASH_2)));
        assertThat(transactionService.isKnownByLnd(TRANSACTION_HASH)).contains(false);
    }

    @Test
    void isKnownByLnd() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        when(grpcTransactions.getKnownTransactionHashesInBlock(TRANSACTION.blockHeight())).thenReturn(
                Optional.of(Set.of(TRANSACTION_HASH_2, TRANSACTION_HASH))
        );
        assertThat(transactionService.isKnownByLnd(TRANSACTION_HASH)).contains(true);
    }

    @Test
    void getTransaction_empty() {
        assertThat(transactionService.getTransaction(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void getTransaction_known_in_dao() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(transactionService.getTransaction(TRANSACTION_HASH)).contains(TRANSACTION);
        verify(transactionDao, never()).saveTransaction(any());
        verify(transactionProvider, never()).get(any());
    }

    @Test
    void getTransaction_unknown_in_dao_successful_download() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        when(transactionProvider.get(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(transactionService.getTransaction(TRANSACTION_HASH)).contains(TRANSACTION);
        verify(transactionDao).saveTransaction(TRANSACTION);
    }

    @Test
    void getTransaction_unknown_in_dao_failed_download() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        when(transactionProvider.get(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(transactionService.getTransaction(TRANSACTION_HASH)).isEmpty();
        verify(transactionDao, never()).saveTransaction(any());
    }

    @Test
    void isKnown_false() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(transactionService.isKnown(TRANSACTION_HASH)).isFalse();
        assertThat(transactionService.isUnknown(TRANSACTION_HASH)).isTrue();
    }

    @Test
    void isKnown_true() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(transactionService.isKnown(TRANSACTION_HASH)).isTrue();
        assertThat(transactionService.isUnknown(TRANSACTION_HASH)).isFalse();
    }
}
