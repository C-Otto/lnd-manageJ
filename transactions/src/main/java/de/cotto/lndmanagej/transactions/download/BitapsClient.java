package de.cotto.lndmanagej.transactions.download;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(value = "bitaps", url = "https://api.bitaps.com")
@RateLimiter(name = "bitaps")
@CircuitBreaker(name = "bitaps")
public interface BitapsClient extends TransactionDetailsClient {
    @Override
    @GetMapping("/btc/v1/blockchain/transaction/{transactionHash}")
    Optional<BitapsTransactionDto> getTransaction(@PathVariable String transactionHash);
}
