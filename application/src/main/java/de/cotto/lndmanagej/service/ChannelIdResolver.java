package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.transactions.model.Transaction;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChannelIdResolver {
    private final TransactionService transactionService;

    public ChannelIdResolver(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Optional<ChannelId> resolve(ChannelPoint channelPoint) {
        return transactionService.getTransaction(channelPoint.getTransactionHash())
                .map(transaction -> getChannelId(transaction, channelPoint));
    }

    private ChannelId getChannelId(Transaction transaction, ChannelPoint channelPoint) {
        int block = transaction.blockHeight();
        int transactionIndex = transaction.positionInBlock();
        int output = channelPoint.getOutput();
        return ChannelId.fromCompactForm(block + ":" + transactionIndex + ":" + output);
    }
}
