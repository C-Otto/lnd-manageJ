package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class ChannelIdConverter implements Converter<String, ChannelId> {
    private final ChannelIdResolver channelIdResolver;

    public ChannelIdConverter(ChannelIdResolver channelIdResolver) {
        this.channelIdResolver = channelIdResolver;
    }

    @Override
    public ChannelId convert(@Nonnull String source) {
        try {
            return fromShortChannelId(source);
        } catch (NumberFormatException numberFormatException) {
            return fromCompactFormOrChannelPoint(source);
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Optional<ChannelId> tryToConvert(String source) {
        try {
            return Optional.of(fromShortChannelId(source));
        } catch (IllegalArgumentException e) {
            // ignore
        }
        try {
            return Optional.of(fromCompactFormOrChannelPoint(source));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private ChannelId fromCompactFormOrChannelPoint(String source) {
        try {
            return ChannelId.fromCompactForm(source);
        } catch (IllegalArgumentException e) {
            return fromChannelPoint(source);
        }
    }

    private ChannelId fromChannelPoint(String source) {
        return channelIdResolver.resolveFromChannelPoint(ChannelPoint.create(source))
                .orElseThrow(IllegalArgumentException::new);
    }

    private ChannelId fromShortChannelId(String source) {
        long shortChannelId = Long.parseLong(source);
        return ChannelId.fromShortChannelId(shortChannelId);
    }
}
