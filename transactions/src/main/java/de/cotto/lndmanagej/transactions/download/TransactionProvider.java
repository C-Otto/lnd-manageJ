package de.cotto.lndmanagej.transactions.download;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Network;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.model.Transaction;
import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionProvider {
    private final GrpcGetInfo grpcGetInfo;
    private final List<TransactionDetailsClient> clients;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TransactionProvider(GrpcGetInfo grpcGetInfo, List<TransactionDetailsClient> clients) {
        this.grpcGetInfo = grpcGetInfo;
        this.clients = clients;
    }

    public Optional<Transaction> get(TransactionHash transactionHash) {
        return getAndHandleExceptions(transactionHash).map(TransactionDto::toModel);
    }

    private Optional<? extends TransactionDto> getAndHandleExceptions(TransactionHash transactionHash) {
        List<TransactionDetailsClient> randomizedClients = new ArrayList<>(clients);
        Collections.shuffle(randomizedClients);
        return randomizedClients.stream()
                .map(client -> getWithClient(transactionHash, client))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<? extends TransactionDto> getWithClient(
            TransactionHash transactionHash,
            TransactionDetailsClient client
    ) {
        try {
            Network network = grpcGetInfo.getNetwork().orElse(null);
            if (network == null) {
                return Optional.empty();
            }
            if (network == Network.TESTNET) {
                return client.getTransactionTestnet(transactionHash.getHash());
            } else if (network == Network.MAINNET) {
                return client.getTransaction(transactionHash.getHash());
            } else {
                return Optional.empty();
            }
        } catch (FeignException feignException) {
            logger.warn("Feign exception: ", feignException);
            return Optional.empty();
        } catch (RequestNotPermitted requestNotPermitted) {
            logger.warn("Blockcypher is rate limited: ", requestNotPermitted);
            return Optional.empty();
        }
    }

}
