package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenInitiatorResolverImplTest {
    @InjectMocks
    private OpenInitiatorResolverImpl openInitiatorResolverImpl;

    @Mock
    private TransactionService transactionService;

    @Test
    void transaction_not_known() {
        when(transactionService.isKnownByLnd(TRANSACTION_HASH)).thenReturn(Optional.of(false));
        assertThat(openInitiatorResolverImpl.resolveFromOpenTransactionHash(TRANSACTION_HASH))
                .isEqualTo(OpenInitiator.REMOTE);
    }

    @Test
    void transaction_known() {
        when(transactionService.isKnownByLnd(TRANSACTION_HASH)).thenReturn(Optional.of(true));
        assertThat(openInitiatorResolverImpl.resolveFromOpenTransactionHash(TRANSACTION_HASH))
                .isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void unable_to_determine_transaction_status() {
        when(transactionService.isKnownByLnd(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(openInitiatorResolverImpl.resolveFromOpenTransactionHash(TRANSACTION_HASH))
                .isEqualTo(OpenInitiator.UNKNOWN);
    }
}