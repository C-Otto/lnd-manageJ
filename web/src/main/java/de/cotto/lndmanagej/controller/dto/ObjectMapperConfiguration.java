package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.ZonedDateTime;

@Configuration
public class ObjectMapperConfiguration {
    public ObjectMapperConfiguration() {
        // default constructor
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule("SimpleModule");
        module.addSerializer(Pubkey.class, new ToStringSerializer());
        module.addSerializer(ChannelId.class, new ToStringSerializer());
        module.addSerializer(ChannelPoint.class, new ToStringSerializer());
        module.addSerializer(ZonedDateTime.class, new ToStringSerializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }
}
