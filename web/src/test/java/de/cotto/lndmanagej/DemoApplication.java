package de.cotto.lndmanagej;

import de.cotto.lndmanagej.demo.DemoDataService;
import de.cotto.lndmanagej.ui.UiDataService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {"de.cotto.lndmanagej.ui"})
public class DemoApplication {

    public DemoApplication() {
        // to satisfy pmd
    }

    @Bean
    public UiDataService demoData() {
        return new DemoDataService();
    }

    public static void main(String[] arguments) {
        SpringApplication.run(DemoApplication.class, arguments);
    }
}