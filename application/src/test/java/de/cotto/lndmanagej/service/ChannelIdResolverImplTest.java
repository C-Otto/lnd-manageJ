package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.OUTPUT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.warn;

@ExtendWith(MockitoExtension.class)
class ChannelIdResolverImplTest {
    private final TestLogger logger = TestLoggerFactory.getTestLogger(ChannelIdResolverImpl.class);

    @InjectMocks
    private ChannelIdResolverImpl channelIdResolver;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        logger.clearAll();
    }

    @Test
    void unknown() {
        when(transactionService.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).isEmpty();
        assertThat(logger.getLoggingEvents()).contains(
                warn("Unable resolve transaction ID for {}", TRANSACTION_HASH)
        );
    }

    @Test
    void known() {
        ChannelId expectedChannelId = ChannelId.fromCompactForm(BLOCK_HEIGHT + ":" + POSITION_IN_BLOCK + ":" + OUTPUT);
        when(transactionService.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.of(TRANSACTION));
        assertThat(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).contains(expectedChannelId);
        assertThat(logger.getLoggingEvents()).isEmpty();
    }
}