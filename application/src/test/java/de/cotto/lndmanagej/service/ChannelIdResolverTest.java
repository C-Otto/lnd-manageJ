package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.OUTPUT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelIdResolverTest {
    @InjectMocks
    private ChannelIdResolver channelIdResolver;

    @Mock
    private TransactionService transactionService;

    @Test
    void unknown() {
        when(transactionService.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(channelIdResolver.resolve(CHANNEL_POINT)).isEmpty();
    }

    @Test
    void known() {
        ChannelId expectedChannelId = ChannelId.fromCompactForm(BLOCK_HEIGHT + ":" + POSITION_IN_BLOCK + ":" + OUTPUT);
        when(transactionService.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(channelIdResolver.resolve(CHANNEL_POINT)).contains(expectedChannelId);
    }
}