package de.cotto.lndmanagej.model;

import java.util.Set;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;
    private final BalanceInformation balanceInformation;

    public LocalChannel(Channel channel, Pubkey ownPubkey, BalanceInformation balanceInformation) {
        super(channel.getId(), channel.getCapacity(), channel.getPubkeys());
        Set<Pubkey> pubkeys = channel.getPubkeys();
        remotePubkey = pubkeys.stream()
                .filter(pubkey -> !ownPubkey.equals(pubkey))
                .findFirst()
                .orElseThrow();
        if (!pubkeys.contains(ownPubkey)) {
            throw new IllegalArgumentException("Channel must have given pubkey as peer");
        }
        this.balanceInformation = balanceInformation;
    }

    public Pubkey getRemotePubkey() {
        return remotePubkey;
    }

    public BalanceInformation getBalanceInformation() {
        return balanceInformation;
    }
}
