package de.cotto.lndmanagej.transactions.download;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "blockcypher", url = "https://api.blockcypher.com")
@RateLimiter(name = "blockcypher")
@CircuitBreaker(name = "blockcypher")
public interface BlockcypherClient {
    @GetMapping("/v1/btc/main/txs/{transactionHash}")
    Optional<BlockcypherTransactionDto> getTransaction(@PathVariable String transactionHash);
}
