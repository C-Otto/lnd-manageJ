package de.cotto.lndmanagej.model;

public class ClosedChannel extends LocalChannel {
    public ClosedChannel(Channel channel, Pubkey ownPubkey) {
        super(channel, ownPubkey);
    }
}
