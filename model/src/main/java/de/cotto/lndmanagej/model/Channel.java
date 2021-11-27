package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

public class Channel {
    private final ChannelCoreInformation channelCoreInformation;
    private final Set<Pubkey> pubkeys;

    protected Channel(ChannelCoreInformation channelCoreInformation, Pubkey pubkey1, Pubkey pubkey2) {
        this.channelCoreInformation = channelCoreInformation;
        if (pubkey1.equals(pubkey2)) {
            throw new IllegalArgumentException("Pubkeys must not be the same");
        }
        this.pubkeys = Set.of(pubkey1, pubkey2);
    }

    public Coins getCapacity() {
        return channelCoreInformation.capacity();
    }

    public ChannelId getId() {
        return channelCoreInformation.channelId();
    }

    public Set<Pubkey> getPubkeys() {
        return pubkeys;
    }

    public ChannelPoint getChannelPoint() {
        return channelCoreInformation.channelPoint();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Channel channel = (Channel) other;
        return Objects.equals(channelCoreInformation, channel.channelCoreInformation)
                && Objects.equals(pubkeys, channel.pubkeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelCoreInformation, pubkeys);
    }
}
