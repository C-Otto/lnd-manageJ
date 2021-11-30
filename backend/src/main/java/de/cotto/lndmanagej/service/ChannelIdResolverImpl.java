package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.transactions.model.Transaction;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChannelIdResolverImpl implements ChannelIdResolver {
    private final TransactionService transactionService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ChannelIdResolverImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Timed
    @Override
    public Optional<ChannelId> resolveFromChannelPoint(ChannelPoint channelPoint) {
        String transactionHash = channelPoint.getTransactionHash();
        Optional<Transaction> transactionOptional = transactionService.getTransaction(transactionHash);
        if (transactionOptional.isEmpty()) {
            logger.warn("Unable resolve transaction ID for {}", transactionHash);
        }
        return transactionOptional.map(transaction -> getChannelId(transaction, channelPoint));
    }

    private ChannelId getChannelId(Transaction transaction, ChannelPoint channelPoint) {
        int block = transaction.blockHeight();
        int transactionIndex = transaction.positionInBlock();
        int output = channelPoint.getOutput();
        return ChannelId.fromCompactForm(block + ":" + transactionIndex + ":" + output);
    }
}
