package de.cotto.lndmanagej.model;

import java.util.Set;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;

    protected LocalChannel(Channel channel, Pubkey ownPubkey) {
        super(channel.getId(), channel.getCapacity(), channel.getPubkeys());
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
}
