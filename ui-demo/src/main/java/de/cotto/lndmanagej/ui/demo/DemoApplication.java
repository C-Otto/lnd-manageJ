package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.controller.PubkeyConverter;
import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.StatusServiceImpl;
import de.cotto.lndmanagej.ui.UiDataServiceImpl;
import de.cotto.lndmanagej.ui.WarningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
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
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UiDataServiceImpl.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = StatusServiceImpl.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WarningServiceImpl.class)
        }
)
@Import({ChannelIdConverter.class, PubkeyConverter.class, ChannelIdParser.class})
@SuppressWarnings("PMD.UseUtilityClass")
public class DemoApplication {

    public DemoApplication() {
        // default constructor
    }

    static void main(String[] arguments) {
        SpringApplication.run(DemoApplication.class, arguments);
    }
}
