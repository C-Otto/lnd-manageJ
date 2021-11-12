package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class ChannelIdConverter implements Converter<String, ChannelId> {
    public ChannelIdConverter() {
        // default constructor
    }

    @Override
    public ChannelId convert(@Nonnull String source) {
        try {
            long shortChannelId = Long.parseLong(source);
            return ChannelId.fromShortChannelId(shortChannelId);
        } catch (NumberFormatException numberFormatException) {
            return ChannelId.fromCompactForm(source);
        }
    }
}
