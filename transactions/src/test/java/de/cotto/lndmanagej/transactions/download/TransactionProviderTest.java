package de.cotto.lndmanagej.transactions.download;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.download.BlockcypherTransactionDtoFixtures.BLOCKCYPHER_TRANSACTION;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionProviderTest {
    @InjectMocks
    private TransactionProvider transactionProvider;

    @Mock
    private BlockcypherClient blockcypherClient;

    @Test
    void get_empty() {
        assertThat(transactionProvider.get(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void get_feign_exception() {
        when(blockcypherClient.getTransaction(any())).thenThrow(FeignException.class);
        assertThat(transactionProvider.get(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void get_rate_limited_exception() {
        when(blockcypherClient.getTransaction(any())).thenThrow(RequestNotPermitted.class);
        assertThat(transactionProvider.get(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void get() {
        when(blockcypherClient.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(BLOCKCYPHER_TRANSACTION));
        assertThat(transactionProvider.get(TRANSACTION_HASH)).contains(TRANSACTION);
    }
}