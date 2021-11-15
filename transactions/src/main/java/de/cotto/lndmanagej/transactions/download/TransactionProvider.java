package de.cotto.lndmanagej.transactions.download;

import de.cotto.lndmanagej.transactions.model.Transaction;
import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransactionProvider {
    private final BlockcypherClient blockcypherClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TransactionProvider(BlockcypherClient blockcypherClient) {
        this.blockcypherClient = blockcypherClient;
    }

    public Optional<Transaction> get(String transactionHash) {
        return getAndHandleExceptions(transactionHash)
                .map(BlockcypherTransactionDto::toModel);
    }

    private Optional<BlockcypherTransactionDto> getAndHandleExceptions(String transactionHash) {
        try {
            return blockcypherClient.getTransaction(transactionHash);
        } catch (FeignException feignException) {
            logger.warn("Feign exception: ", feignException);
            return Optional.empty();
        } catch (RequestNotPermitted requestNotPermitted) {
            logger.warn("Blockcypher is rate limited: ", requestNotPermitted);
            return Optional.empty();
        }
    }

}
