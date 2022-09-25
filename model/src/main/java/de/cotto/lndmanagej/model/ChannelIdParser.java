package de.cotto.lndmanagej.model;

import org.springframework.stereotype.Component;

@Component
public class ChannelIdParser {
    private final ChannelIdResolver channelIdResolver;

    public ChannelIdParser(ChannelIdResolver channelIdResolver) {
        this.channelIdResolver = channelIdResolver;
    }

    public ChannelId parseFromString(String source) {
        try {
            return fromShortChannelId(source);
        } catch (NumberFormatException numberFormatException) {
            return fromCompactFormOrChannelPoint(source);
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
