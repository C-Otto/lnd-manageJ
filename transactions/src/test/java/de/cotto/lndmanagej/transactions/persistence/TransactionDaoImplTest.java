package de.cotto.lndmanagej.transactions.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static de.cotto.lndmanagej.transactions.persistence.TransactionJpaDtoFixtures.TRANSACTION_JPA_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDaoImplTest {

    @InjectMocks
    private TransactionDaoImpl transactionDao;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void getTransaction_unknown() {
        assertThat(transactionDao.getTransaction(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void getTransaction() {
        when(transactionRepository.findById(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION_JPA_DTO));

        assertThat(transactionDao.getTransaction(TRANSACTION_HASH)).contains(TRANSACTION);
    }

    @Test
    void saveTransaction() {
        transactionDao.saveTransaction(TRANSACTION);
        verify(transactionRepository).save(argThat(dto -> TRANSACTION_HASH.equals(dto.getHash())));
        verify(transactionRepository).save(argThat(dto -> BLOCK_HEIGHT == dto.getBlockHeight()));
        verify(transactionRepository).save(argThat(dto -> POSITION_IN_BLOCK == dto.getPositionInBlock()));
        verify(transactionRepository).save(argThat(dto -> FEES.satoshis() == dto.getFees()));
    }
}