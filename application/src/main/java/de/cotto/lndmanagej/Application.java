package de.cotto.lndmanagej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    public static void main(String[] arguments) {
        SpringApplication.run(Application.class, arguments);
    }
}
