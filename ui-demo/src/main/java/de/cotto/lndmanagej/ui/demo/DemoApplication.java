package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.controller.PubkeyConverter;
import de.cotto.lndmanagej.ui.UiDataService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {"de.cotto.lndmanagej.ui"})
@Import({ChannelIdConverter.class, PubkeyConverter.class})
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
