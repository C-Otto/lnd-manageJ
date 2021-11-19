package de.cotto.lndmanagej.model;

public interface OpenInitiatorResolver {
    OpenInitiator resolveFromOpenTransactionHash(String transactionHash);
}
