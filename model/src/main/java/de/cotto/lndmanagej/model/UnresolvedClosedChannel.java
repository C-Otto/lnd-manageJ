package de.cotto.lndmanagej.model;

public class UnresolvedClosedChannel extends LocalChannel {
    private final Pubkey ownPubkey;

    public UnresolvedClosedChannel(Channel channel, Pubkey ownPubkey) {
        super(channel, ownPubkey);
        this.ownPubkey = ownPubkey;
    }

    @Override
    public LocalChannel getWithId(ChannelId channelId) {
        return new UnresolvedClosedChannel(super.getWithId(channelId), ownPubkey);
    }

    public Pubkey getOwnPubkey() {
        return ownPubkey;
    }
}
