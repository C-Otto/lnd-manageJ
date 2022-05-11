package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.controller.PubkeyConverter;
import de.cotto.lndmanagej.ui.UiDataServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan(
        basePackages = {"de.cotto.lndmanagej.ui"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UiDataServiceImpl.class)}
)
@Import({ChannelIdConverter.class, PubkeyConverter.class})
@SuppressWarnings("PMD.UseUtilityClass")
public class DemoApplication {

    public DemoApplication() {
        // default constructor
    }

    public static void main(String[] arguments) {
        SpringApplication.run(DemoApplication.class, arguments);
    }
}
