package de.cotto.lndmanagej;

import de.cotto.lndmanagej.ui.formatting.Formatter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootConfiguration {
    public SpringBootConfiguration() {
        // default constructor
    }

    @Bean
    public Formatter formatter() {
        return new Formatter();
    }
}
