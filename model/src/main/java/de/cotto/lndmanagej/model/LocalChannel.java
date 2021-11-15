package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;

    protected LocalChannel(Channel channel, Pubkey ownPubkey) {
        super(channel.getId(), channel.getCapacity(), channel.getChannelPoint(), channel.getPubkeys());
        Set<Pubkey> pubkeys = channel.getPubkeys();
        remotePubkey = pubkeys.stream()
                .filter(pubkey -> !ownPubkey.equals(pubkey))
                .findFirst()
                .orElseThrow();
        if (!pubkeys.contains(ownPubkey)) {
            throw new IllegalArgumentException("Channel must have given pubkey as peer");
        }
    }

    public Pubkey getRemotePubkey() {
        return remotePubkey;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        LocalChannel that = (LocalChannel) other;
        return Objects.equals(remotePubkey, that.remotePubkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), remotePubkey);
    }
}
