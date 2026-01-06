package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.time.ZonedDateTime;

@Configuration
public class ObjectMapperConfiguration {
    public ObjectMapperConfiguration() {
        // default constructor
    }

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        SimpleModule module = new SimpleModule("SimpleModule");
        module.addSerializer(Pubkey.class, new ToStringSerializer(Pubkey.class));
        module.addSerializer(ChannelId.class, new ToStringSerializer(ChannelId.class));
        module.addSerializer(ChannelPoint.class, new ToStringSerializer(ChannelPoint.class));
        module.addSerializer(ZonedDateTime.class, new ToStringSerializer(ZonedDateTime.class));
        module.addSerializer(Instant.class, new ToStringSerializer(Instant.class));

        DefaultPrettyPrinter prettyPrinter =
                new DefaultPrettyPrinter().withObjectIndenter(
                        new DefaultIndenter().withLinefeed("\n")
                );

        return builder -> builder
                .enable(SerializationFeature.INDENT_OUTPUT)
                .addModule(module)
                .defaultPrettyPrinter(prettyPrinter);
    }
}
