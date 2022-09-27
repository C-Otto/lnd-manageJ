package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdParser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class ChannelIdConverter implements Converter<String, ChannelId> {
    private final ChannelIdParser channelIdParser;

    public ChannelIdConverter(ChannelIdParser channelIdParser) {
        this.channelIdParser = channelIdParser;
    }

    @Override
    public ChannelId convert(@Nonnull String source) {
        return channelIdParser.parseFromString(source);
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Optional<ChannelId> tryToConvert(String source) {
        try {
            return Optional.ofNullable(convert(source));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
