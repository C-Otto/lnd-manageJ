package de.cotto.lndmanagej.model;

import java.util.Set;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;
    private final Coins localBalance;
    private final Coins localReserve;

    public LocalChannel(Channel channel, Pubkey ownPubkey, Coins localBalance, Coins localReserve) {
        super(channel.getId(), channel.getCapacity(), channel.getPubkeys());
        this.localBalance = localBalance;
        this.localReserve = localReserve;
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

    public Coins getLocalBalance() {
        return localBalance;
    }

    public Coins getLocalReserve() {
        return localReserve;
    }
}
