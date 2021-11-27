package de.cotto.lndmanagej.model;

import java.util.Objects;

public class LocalOpenChannel extends LocalChannel {
    private final BalanceInformation balanceInformation;
    private final Coins totalSent;
    private final Coins totalReceived;
    private final boolean active;

    public LocalOpenChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            BalanceInformation balanceInformation,
            OpenInitiator openInitiator,
            Coins totalSent,
            Coins totalReceived,
            boolean isPrivate,
            boolean active
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, openInitiator, isPrivate);
        this.balanceInformation = balanceInformation;
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
        this.active = active;
    }

    public BalanceInformation getBalanceInformation() {
        return balanceInformation;
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(isPrivateChannel(), active, false, OpenCloseStatus.OPEN);
    }

    @Override
    public Coins getTotalSent() {
        return totalSent;
    }

    @Override
    public Coins getTotalReceived() {
        return totalReceived;
    }

    @Override
    @SuppressWarnings("CPD-START")
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
        LocalOpenChannel that = (LocalOpenChannel) other;
        return active == that.active
                && Objects.equals(balanceInformation, that.balanceInformation)
                && Objects.equals(totalSent, that.totalSent)
                && Objects.equals(totalReceived, that.totalReceived);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balanceInformation, totalSent, totalReceived, active);
    }
}