package de.cotto.lndmanagej.model;

public interface OpenInitiatorResolver {
    OpenInitiator resolveFromOpenTransactionHash(TransactionHash transactionHash);
}
