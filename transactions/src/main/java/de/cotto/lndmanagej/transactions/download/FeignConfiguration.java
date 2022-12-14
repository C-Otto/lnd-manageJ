package de.cotto.lndmanagej.transactions.download;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients
@ImportAutoConfiguration(FeignAutoConfiguration.class)
public class FeignConfiguration {
    public FeignConfiguration() {
        // default constructor
    }
}
