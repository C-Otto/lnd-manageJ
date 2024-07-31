package de.cotto.lndmanagej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    public Application() {
        // default constructor
    }

    public static void main(String[] arguments) {
        SpringApplication.run(Application.class, arguments);
    }

    @Bean
    public FlywayConfigurationCustomizer flywayCustomizer() {
        return configuration -> configuration.configuration(Map.of("flyway.postgresql.transactional.lock", "false"));
    }
}
