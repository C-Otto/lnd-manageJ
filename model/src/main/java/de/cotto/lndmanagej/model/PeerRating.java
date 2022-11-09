package de.cotto.lndmanagej.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PeerRating implements Rating {
    private final Pubkey pubkey;
    private final List<ChannelRating> channelRatings;

    private PeerRating(Pubkey pubkey, List<ChannelRating> channelRatings) {
        this.pubkey = pubkey;
        this.channelRatings = channelRatings;
    }

    public static PeerRating forPeer(Pubkey pubkey) {
        return new PeerRating(pubkey, List.of());
    }

    @Override
    public long getValue() {
        return channelRatings.stream().mapToLong(ChannelRating::getValue).sum();
    }

    @Override
    public Map<String, Number> getDescriptions() {
        if (channelRatings.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Number> descriptions = new LinkedHashMap<>();
        channelRatings.stream().map(ChannelRating::getDescriptions).forEach(descriptions::putAll);
        descriptions.put(pubkey + " rating", getValue());
        return Collections.unmodifiableMap(descriptions);
    }

    public PeerRating withChannelRating(ChannelRating channelRating) {
        List<ChannelRating> allRatings = new ArrayList<>(channelRatings);
        allRatings.add(channelRating);
        return new PeerRating(pubkey, allRatings);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PeerRating that = (PeerRating) other;
        return Objects.equals(pubkey, that.pubkey) && Objects.equals(channelRatings, that.channelRatings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pubkey, channelRatings);
    }
}
