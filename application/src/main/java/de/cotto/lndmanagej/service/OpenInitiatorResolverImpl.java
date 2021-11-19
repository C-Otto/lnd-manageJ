package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.OpenInitiatorResolver;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.stereotype.Component;

@Component
public class OpenInitiatorResolverImpl implements OpenInitiatorResolver {
    private final TransactionService transactionService;

    public OpenInitiatorResolverImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public OpenInitiator resolveFromOpenTransactionHash(String transactionHash) {
        Boolean knownByLnd = transactionService.isKnownByLnd(transactionHash).orElse(null);
        if (knownByLnd == null) {
            return OpenInitiator.UNKNOWN;
        }
        if (knownByLnd) {
            return OpenInitiator.LOCAL;
        }
        return OpenInitiator.REMOTE;
    }
}
