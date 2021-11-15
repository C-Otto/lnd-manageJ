package de.cotto.lndmanagej.model;

public class UnresolvedClosedChannel extends LocalChannel {
    public UnresolvedClosedChannel(Channel channel, Pubkey ownPubkey) {
        super(channel, ownPubkey);
    }
}
